buildscript {
	ext {
		springBootVersion = '2.1.1.RELEASE'
	}
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}

apply plugin: 'java'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
apply plugin:'application'

mainClassName = "{{pack}}.{{className}}WebApplication"

def ngoyVersion = '{{ngoyVersion}}';

group = '{{pack}}'
version = '0.0.1'
sourceCompatibility = 1.8

repositories {
	mavenCentral()
	maven { url "https://jitpack.io" }
}

dependencies {
	implementation('org.springframework.boot:spring-boot-starter-web')
	implementation("com.github.krizzdewizz:ngoy:$ngoyVersion")
}

// include component html/css files that reside next to the class
sourceSets {
  main {
    resources {
      srcDir 'src/main/java'
    }
  }
}

// extract ngoy jar to use it's cli
configurations { ngoy }
dependencies { ngoy "com.github.krizzdewizz:ngoy:$ngoyVersion" }
task extractNgoy(type: Copy) {
    into "$projectDir/build/tmp/ngoy-$ngoyVersion"
    from configurations.ngoy
}
