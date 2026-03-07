package com.lifeflow.security

import com.lifeflow.domain.model.LifeFlowIdentity
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.lang.Byte as JavaByte

class EncryptedIdentityRepositoryTest {

    @Test
    fun `serialize and deserialize round trip preserves identity fields`() {
        val repo = newRepositoryForInternalTests()
        val identity = LifeFlowIdentity(
            id = UUID.randomUUID(),
            createdAtEpochMillis = 1_717_171_717L,
            isActive = true
        )

        val bytes = invokePrivate<ByteArray>(
            target = repo,
            methodName = "serialize",
            parameterTypes = arrayOf(LifeFlowIdentity::class.java),
            args = arrayOf(identity)
        )

        val restored = invokePrivate<LifeFlowIdentity>(
            target = repo,
            methodName = "deserialize",
            parameterTypes = arrayOf(ByteArray::class.java),
            args = arrayOf(bytes)
        )

        assertEquals(identity.id, restored.id)
        assertEquals(identity.createdAtEpochMillis, restored.createdAtEpochMillis)
        assertEquals(identity.isActive, restored.isActive)
    }

    @Test
    fun `deserialize rejects payload with missing fields`() {
        val repo = newRepositoryForInternalTests()
        val invalid = "only|two".toByteArray(StandardCharsets.UTF_8)

        val error = expectFailure {
            invokePrivate<LifeFlowIdentity>(
                target = repo,
                methodName = "deserialize",
                parameterTypes = arrayOf(ByteArray::class.java),
                args = arrayOf(invalid)
            )
        }

        assertTrue(error is IllegalArgumentException)
        assertTrue(error.message.orEmpty().contains("Invalid identity payload"))
    }

    @Test
    fun `deserialize rejects invalid active flag`() {
        val repo = newRepositoryForInternalTests()
        val invalid = "${UUID.randomUUID()}|123456|not_boolean".toByteArray(StandardCharsets.UTF_8)

        val error = expectFailure {
            invokePrivate<LifeFlowIdentity>(
                target = repo,
                methodName = "deserialize",
                parameterTypes = arrayOf(ByteArray::class.java),
                args = arrayOf(invalid)
            )
        }

        assertTrue(error is IllegalArgumentException)
        assertTrue(error.message.orEmpty().contains("Invalid identity active flag"))
    }

    @Test
    fun `deserialize rejects invalid uuid format`() {
        val repo = newRepositoryForInternalTests()
        val invalid = "not-a-uuid|123456|true".toByteArray(StandardCharsets.UTF_8)

        val error = expectFailure {
            invokePrivate<LifeFlowIdentity>(
                target = repo,
                methodName = "deserialize",
                parameterTypes = arrayOf(ByteArray::class.java),
                args = arrayOf(invalid)
            )
        }

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun `wrap and unwrap preserve explicit versioned scheme marker`() {
        val repo = newRepositoryForInternalTests()
        val versionedMarker = privateByteConstant("SCHEME_VERSIONED")
        val cipher = byteArrayOf(10, 20, 30, 40)

        val wrapped = invokePrivate<ByteArray>(
            target = repo,
            methodName = "wrapWithSchemeMarker",
            parameterTypes = arrayOf(JavaByte.TYPE, ByteArray::class.java),
            args = arrayOf(versionedMarker, cipher)
        )

        val unwrapped = invokePrivate<Pair<Byte, ByteArray>>(
            target = repo,
            methodName = "unwrapSchemeMarker",
            parameterTypes = arrayOf(ByteArray::class.java),
            args = arrayOf(wrapped)
        )

        assertEquals(versionedMarker, wrapped[0])
        assertEquals(versionedMarker, unwrapped.first)
        assertArrayEquals(cipher, unwrapped.second)
    }

    @Test
    fun `unwrap treats unmarked blob as legacy format`() {
        val repo = newRepositoryForInternalTests()
        val legacyMarker = privateByteConstant("SCHEME_LEGACY")
        val legacyCipher = byteArrayOf(1, 2, 3, 4, 5)

        val unwrapped = invokePrivate<Pair<Byte, ByteArray>>(
            target = repo,
            methodName = "unwrapSchemeMarker",
            parameterTypes = arrayOf(ByteArray::class.java),
            args = arrayOf(legacyCipher)
        )

        assertEquals(legacyMarker, unwrapped.first)
        assertArrayEquals(legacyCipher, unwrapped.second)
    }

    @Test
    fun `unwrap rejects empty blob`() {
        val repo = newRepositoryForInternalTests()

        val error = expectFailure {
            invokePrivate<Pair<Byte, ByteArray>>(
                target = repo,
                methodName = "unwrapSchemeMarker",
                parameterTypes = arrayOf(ByteArray::class.java),
                args = arrayOf(ByteArray(0))
            )
        }

        assertTrue(error is IllegalArgumentException)
        assertTrue(error.message.orEmpty().contains("Empty stored blob"))
    }

    @Test
    fun `aad helpers produce deterministic legacy and versioned formats`() {
        val repo = newRepositoryForInternalTests()
        val id = UUID.randomUUID()

        val legacyAad = invokePrivate<ByteArray>(
            target = repo,
            methodName = "aadLegacy",
            parameterTypes = arrayOf(UUID::class.java),
            args = arrayOf(id)
        )
        val versionedAad = invokePrivate<ByteArray>(
            target = repo,
            methodName = "aadVersioned",
            parameterTypes = arrayOf(UUID::class.java, Long::class.javaPrimitiveType!!),
            args = arrayOf(id, 7L)
        )

        assertEquals(id.toString(), String(legacyAad, StandardCharsets.UTF_8))
        assertEquals("${id}|v7", String(versionedAad, StandardCharsets.UTF_8))
    }

    private fun newRepositoryForInternalTests(): EncryptedIdentityRepository {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val theUnsafeField = unsafeClass.getDeclaredField("theUnsafe")
        theUnsafeField.isAccessible = true
        val unsafe = theUnsafeField.get(null)

        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, EncryptedIdentityRepository::class.java) as EncryptedIdentityRepository
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> invokePrivate(
        target: Any,
        methodName: String,
        parameterTypes: Array<Class<*>>,
        args: Array<Any?>
    ): T {
        val method = EncryptedIdentityRepository::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true
        return method.invoke(target, *args) as T
    }

    private fun expectFailure(block: () -> Unit): Throwable {
        return try {
            block()
            fail("Expected failure but block completed successfully.")
            throw AssertionError("Unreachable")
        } catch (t: Throwable) {
            unwrapInvocationFailure(t)
        }
    }

    private fun unwrapInvocationFailure(t: Throwable): Throwable {
        var current = t
        while (current.cause != null &&
            (current::class.java.name == "java.lang.reflect.InvocationTargetException" ||
                current::class.java.name == "java.lang.RuntimeException")
        ) {
            current = current.cause ?: break
        }
        return current
    }

    private fun privateByteConstant(name: String): Byte {
        runCatching {
            val field = EncryptedIdentityRepository::class.java.getDeclaredField(name)
            field.isAccessible = true
            return field.get(null) as Byte
        }

        val companionField = EncryptedIdentityRepository::class.java.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companion = companionField.get(null)

        val companionClass = Class.forName("${EncryptedIdentityRepository::class.java.name}\$Companion")
        val field = companionClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(companion) as Byte
    }
}