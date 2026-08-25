cmake_minimum_required(VERSION 3.21)
project(Vape421Native VERSION 1.0 LANGUAGES C RC)

# =========================================================================
# Vape421Native.dll — quarantined / networking-disabled build
# =========================================================================
# This CMake file builds ONLY the x64 DLL and injector.  No network
# libraries are linked.  The DLL calls zero socket/Winsock/WinHTTP APIs.
# See README.md for the full verification checklist.
# =========================================================================

if(NOT WIN32)
    message(FATAL_ERROR "Windows x64 only")
endif()
if(NOT MSVC)
    message(FATAL_ERROR "MSVC x64 toolchain required")
endif()
if(NOT CMAKE_SIZEOF_VOID_P EQUAL 8)
    message(FATAL_ERROR "x64 only")
endif()

# --- JDK for JNI/JVMTI headers ------------------------------------------
set(VAPE421_JAVA_HOME "$ENV{JAVA_HOME}" CACHE PATH
        "JDK containing include/jni.h and include/jvmti.h")
if(NOT VAPE421_JAVA_HOME)
    foreach(candidate
            "C:/Program Files/Java/jdk1.8.0_301"
            "C:/Program Files/Java/jdk-17")
        if(EXISTS "${candidate}/include/jni.h")
            set(VAPE421_JAVA_HOME "${candidate}")
            break()
        endif()
    endforeach()
endif()
find_path(VAPE421_JNI_INCLUDE  jni.h  HINTS "${VAPE421_JAVA_HOME}/include" REQUIRED)
find_path(VAPE421_JNI_MD_INCLUDE jni_md.h HINTS "${VAPE421_JAVA_HOME}/include/win32" REQUIRED)
find_path(VAPE421_JVMTI_INCLUDE jvmti.h HINTS "${VAPE421_JAVA_HOME}/include" REQUIRED)

# --- Embedded product JAR ------------------------------------------------
set(VAPE421_PRODUCT_JAR "" CACHE FILEPATH
        "Path to the quarantined injection JAR (build with ../gradlew injectionJar)")
if(NOT VAPE421_PRODUCT_JAR OR NOT EXISTS "${VAPE421_PRODUCT_JAR}")
    message(FATAL_ERROR
            "VAPE421_PRODUCT_JAR must point to the injection JAR.\n"
            "Build it first:  cd .. && .\\gradlew.bat injectionJar\n"
            "Then pass:       -DVAPE421_PRODUCT_JAR=..\\build\\libs\\vape421-...-injection.jar")
endif()

# Embed the JAR as a Windows resource (type RCDATA, id 421)
get_filename_component(VAPE421_PRODUCT_JAR "${VAPE421_PRODUCT_JAR}" ABSOLUTE)
file(TO_CMAKE_PATH "${VAPE421_PRODUCT_JAR}" VAPE421_PRODUCT_JAR_RC_PATH)
configure_file(payload.rc.in "${CMAKE_CURRENT_BINARY_DIR}/generated/payload.rc" @ONLY)
set_property(SOURCE "${CMAKE_CURRENT_BINARY_DIR}/generated/payload.rc"
        APPEND PROPERTY OBJECT_DEPENDS "${VAPE421_PRODUCT_JAR}")

# --- Vape421Native.dll --------------------------------------------------
add_library(Vape421Native SHARED
        dllmain.c
        loader_bootstrap.c
        native_bridge.c
        "${CMAKE_CURRENT_BINARY_DIR}/generated/payload.rc")

target_include_directories(Vape421Native PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}"
        "${VAPE421_JNI_INCLUDE}"
        "${VAPE421_JNI_MD_INCLUDE}"
        "${VAPE421_JVMTI_INCLUDE}")

target_compile_definitions(Vape421Native PRIVATE
        WIN32_LEAN_AND_MEAN
        _CRT_SECURE_NO_WARNINGS)

target_compile_options(Vape421Native PRIVATE /W4)

# user32 only — window management + clipboard + keyboard/mouse input.
# ws2_32 is intentionally NOT linked (no socket/Winsock API available).
target_link_libraries(Vape421Native PRIVATE user32)

# --- Vape421Injector.exe ------------------------------------------------
add_executable(Vape421Injector injector.c)

target_compile_definitions(Vape421Injector PRIVATE
        WIN32_LEAN_AND_MEAN
        _CRT_SECURE_NO_WARNINGS)

target_compile_options(Vape421Injector PRIVATE /W4)

# --- Output --------------------------------------------------------------
set_property(TARGET Vape421Native Vape421Injector PROPERTY
        MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>DLL")

set_target_properties(Vape421Native Vape421Injector PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/dist")

foreach(configuration Debug Release RelWithDebInfo MinSizeRel)
    string(TOUPPER "${configuration}" configuration_upper)
    set_target_properties(Vape421Native Vape421Injector PROPERTIES
            RUNTIME_OUTPUT_DIRECTORY_${configuration_upper}
            "${CMAKE_BINARY_DIR}/dist")
endforeach()