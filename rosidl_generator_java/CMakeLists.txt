cmake_minimum_required(VERSION 3.5)

project(rosidl_generator_java)

find_package(ament_cmake REQUIRED)

find_package(rcljava_common REQUIRED)

include(CrossCompilingExtra)

ament_export_dependencies(ament_cmake)

ament_python_install_package(${PROJECT_NAME})

if(CMAKE_CROSSCOMPILING)
  find_host_package(Java COMPONENTS Development REQUIRED)
else()
  find_package(Java COMPONENTS Development REQUIRED)
endif()
if(NOT ANDROID)
  find_package(JNI REQUIRED)
endif()
include(UseJava)
include(JavaExtra)

ament_index_register_resource("rosidl_generator_packages")

if(BUILD_TESTING)
  find_package(ament_lint_auto REQUIRED)
  ament_lint_auto_find_test_dependencies()
endif()

install(
  PROGRAMS bin/rosidl_generator_java
  DESTINATION lib/rosidl_generator_java
)

install(
  DIRECTORY cmake resource
  DESTINATION share/${PROJECT_NAME}
)

ament_package(
  CONFIG_EXTRAS "cmake/rosidl_generator_java_get_typesupports.cmake"
    "cmake/register_java.cmake"
    "rosidl_generator_java-extras.cmake.in"
)
