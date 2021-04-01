package com.github.rchargel.build.test

import org.junit.Assert
import org.junit.Test

class BuildTesterTest {

    @Test
    fun mustHaveABuilder() {
        val exc = Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestOne::class.java)
        }
        assert("com.github.rchargel.build.test.TestOne.builder()" == exc.message) {
            "Invalid error"
        }
    }

    @Test
    fun builderMustBeStatic() {
        val exc = Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestTwo::class.java)
        }
        assert("No static method 'builder()'" == exc.message) {
            "Invalid error"
        }
    }

    @Test
    fun builderMustHaveBuildMethod() {
        val exc = Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestThree::class.java).evaluate()
        }
        assert("Builder must have a 'build()' method" == exc.message) { "Invalid Error" }
    }


    @Test
    fun builderBuildsWrongType() {
        val exc = Assert.assertThrows(AssertionError::class.java) {
            BuildTester(TestFour::class.java).evaluate()
        }
        assert("Builder produced object of type class com.github.rchargel.build.test.TestThree but expected class com.github.rchargel.build.test.TestFour" == exc.message) {
            "Invalid Error: ${exc.message}"
        }
    }

    @Test
    fun builderDoesNotSetAllProps() {
        Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestFive::class.java).evaluate()
        }
    }

    @Test
    fun buildTesterCanIgnore() {
        BuildTester(TestFive::class.java, setOf("copyOfName")).evaluate()
    }

    @Test
    fun builderModifiesValue() {
        val exc = Assert.assertThrows(AssertionError::class.java) {
            BuildTester(TestSix::class.java).evaluate()
        }
        assert("name: expected www.pojo.pl but was Hello, www.pojo.pl" == exc.message) {
            exc.message!!
        }
    }

    @Test
    fun builderNotChainable() {
        val exc = Assert.assertThrows(AssertionError::class.java) {
            BuildTester(TestSeven::class.java).evaluate()
        }
        assert("Builder method returns type of void but should be builder" == exc.message) {
            exc.message!!
        }
    }

    @Test
    fun builderBuildsNewInstances() {
        val exc = Assert.assertThrows(AssertionError::class.java) {
            BuildTester(TestEight::class.java).evaluate()
        }
        assert("Builder returns new instance of builder" == exc.message) {
            exc.message!!
        }
    }

    @Test
    fun builderHasTooManyParameters() {
        val exc = Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestNine::class.java).evaluate()
        }
        assert("No method name with parameter class java.lang.String" == exc.message) {
            exc.message!!
        }
    }

    @Test
    fun builderMayUseParentClass() = BuildTester(TestTen::class.java).evaluate()


    @Test
    fun builderHasWrongParameter() {
        val exc = Assert.assertThrows(NoSuchMethodException::class.java) {
            BuildTester(TestEleven::class.java).evaluate()
        }
        assert("No method names with parameter interface java.util.List" == exc.message) {
            exc.message!!
        }
    }
}

data class TestOne(val name: String)
data class TestTwo(val name: String) {
    fun builder() = TestTwo(name)
}

data class TestThree internal constructor(val name: String) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) = apply { this.name = name }

        // invalid build method
        fun buildIt() = TestThree(this.name ?: error("not set"))
    }
}

data class TestFour internal constructor(val name: String) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) = apply { this.name = name }
        fun build() = TestThree(this.name ?: error("not set"))
    }
}

data class TestFive internal constructor(val name: String) {
    val copyOfName = this.name

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) = apply { this.name = name }
        fun build() = TestFive(this.name ?: error("not set"))
    }
}

data class TestSix internal constructor(val name: String) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) = apply { this.name = name }
        fun build() = TestSix("Hello, ${this.name}")
    }
}

data class TestSeven internal constructor(val name: String) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) {
            this.name = name
        }

        fun build() = TestSeven("Hello, ${this.name}")
    }
}


data class TestEight internal constructor(val name: String) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String) = Builder(name)

        fun build() = TestEight("Hello, ${this.name}")
    }
}

data class TestNine internal constructor(val name: String) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var name: String? = null) {
        fun name(name: String, last: String) = apply { this.name = name }

        fun build() = TestNine(this.name ?: error("Not set"))
    }
}

data class TestTen internal constructor(val names: List<String>) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var names: Collection<String>? = null) {
        fun names(names: Collection<String>) = apply { this.names = names }

        fun build() = TestTen(this.names?.toList() ?: emptyList())
    }
}

data class TestEleven internal constructor(val names: List<String>) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor(private var names: Set<String>? = null) {
        fun names(names: Set<String>) = apply { this.names = names }

        fun build() = TestEleven(this.names?.toList() ?: emptyList())
    }
}

