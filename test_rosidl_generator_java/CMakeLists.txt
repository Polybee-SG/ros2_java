cmake_minimum_required(VERSION 3.5)

project(test_rosidl_generator_java)

find_package(ament_cmake REQUIRED)

if(BUILD_TESTING)
  find_package(rcljava_common REQUIRED)
  find_package(rosidl_cmake REQUIRED)
  find_package(test_interface_files REQUIRED)

  find_package(ament_lint_auto REQUIRED)
  ament_lint_auto_find_test_dependencies()

  # TODO(jacobperron): This fixes generating messages with wstrings.
  # Figure out why this is needed as of Foxy, and how we can remove this workaround
  list(APPEND CMAKE_JAVA_COMPILE_FLAGS "-encoding" "ISO-8859-1")

  rosidl_generate_interfaces(${PROJECT_NAME}
    ${test_interface_files_MSG_FILES}
    ${test_interface_files_SRV_FILES}
    action/BasicTypes.action
    action/Nested.action
    SKIP_INSTALL
  )

  set(${PROJECT_NAME}_test_sources
    "src/test/java/org/ros2/generator/InterfacesTest.java"
  )

  set(${PROJECT_NAME}_testsuites
    "org.ros2.generator.InterfacesTest"
  )

  get_property(_${PROJECT_NAME}_messages_jar_file
    TARGET "${PROJECT_NAME}_messages_jar"
    PROPERTY "JAR_FILE")

  set(_deps_library_dirs "")
  list_append_unique(_deps_library_dirs ${CMAKE_CURRENT_BINARY_DIR})
  list_append_unique(_deps_library_dirs ${CMAKE_CURRENT_BINARY_DIR}/rosidl_generator_java/test_rosidl_generator_java/msg/)
  list_append_unique(_deps_library_dirs ${CMAKE_CURRENT_BINARY_DIR}/rosidl_generator_java/test_rosidl_generator_java/srv/)
  list_append_unique(_deps_library_dirs ${CMAKE_CURRENT_BINARY_DIR}/rosidl_generator_java/test_rosidl_generator_java/action/)

  foreach(testsuite ${${PROJECT_NAME}_testsuites})
    ament_add_junit_tests("${PROJECT_NAME}_tests_${testsuite}"
      TIMEOUT 90
      WORKING_DIRECTORY
      "${CMAKE_CURRENT_BINARY_DIR}"
      SOURCES
      "${${PROJECT_NAME}_test_sources}"
      TESTS
      "${testsuite}"
      INCLUDE_JARS
      "${rcljava_common_JARS}"
      "${_${PROJECT_NAME}_messages_jar_file}"
      APPEND_LIBRARY_DIRS
      "${_deps_library_dirs}"
    )

    add_dependencies("${PROJECT_NAME}_tests_${testsuite}" "${PROJECT_NAME}_messages_jar")
  endforeach()
endif()

ament_package()
