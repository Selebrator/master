plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(files("/home/lukas/.src-programs/cvc5/build/install/share/java/cvc5.jar"))
}

tasks.test {
    systemProperty(
            "java.library.path",
            "/home/lukas/.src-programs/cvc5/build/install/lib/"
    )
}

tasks.test {
    useJUnitPlatform()
}
