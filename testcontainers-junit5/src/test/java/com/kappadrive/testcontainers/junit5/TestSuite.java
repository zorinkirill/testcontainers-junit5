package com.kappadrive.testcontainers.junit5;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Use this suite to run all tests in module in IDE.
 * Note: you can directly use "Run All Tests" via gradle in IDEA.
 */
@Suite
@SelectPackages("com.kappadrive.testcontainers.junit5")
@IncludeEngines("junit-jupiter")
@IncludeClassNamePatterns("^.*Tests?$")
class TestSuite {
}
