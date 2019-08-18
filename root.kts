#!/usr/bin/env kscript

import java.io.File

println("Your Android SDK Path:")
val sdkPath = readLine()?.removeSuffix("/")

val adb = "$sdkPath/platform-tools/adb"
val delay = 20

println("\nAVD list:")
execAndWait("$sdkPath/emulator/emulator -list-avds")
println("\nThe name of the AVD you want to install root:")
val avdName = readLine()

exec("$sdkPath/emulator/emulator -avd $avdName -writable-system -selinux disabled -qemu -enable-kvm")

println("Wait for boot...")
println("Waiting $delay seconds to proceed with next step..")

repeat(delay) {
    Thread.sleep(1000)
    print(".")
}

println("\nStarting adb as root")
execAndWait("$adb root")
execAndWait("$adb remount")

println("\nInstalling SuperSU:")
execAndWait("$adb install tools/Superuser.apk")

println("\nInstalling RootChecker:")
execAndWait("$adb install tools/RootChecker.apk")

println("\nWhat's the $avdName architecture? (arm/arm64/armv7/x64/x86)")
val arch = readLine()?.toLowerCase()?.trim()

println("Pushing su:")
execAndWait("$adb push tools/SuperSU/$arch/su /system/xbin/su")
println("Setting permissions:")
execAndWait("$adb shell chmod 0755 /system/xbin/su")

println("\nSetting SELinux permissive...")
execAndWait("$adb shell setenforce 0")
execAndWait("$adb shell su --install")
execAndWait("$adb shell su --daemon&")

println("Finished!")
println("Now open the SuperSU App on AVD, accept the update and wait for complete")
println("If the installation fail, don't be panic. Open RootChecker app and check if root are working.")


fun execAndWait(command: String, dir: File? = null): Int {
    return ProcessBuilder("/bin/sh", "-c", command)
        .redirectErrorStream(true)
        .inheritIO()
        .directory(dir)
        .start()
        .waitFor()
}

fun exec(command: String, dir: File? = null) {
    ProcessBuilder("/bin/sh", "-c", command)
        .redirectErrorStream(true)
        .inheritIO()
        .directory(dir)
        .start()
}
