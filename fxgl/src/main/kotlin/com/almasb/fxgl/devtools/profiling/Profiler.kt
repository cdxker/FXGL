/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxgl.devtools.profiling

import com.almasb.fxgl.core.logging.FXGLLogger
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.service.MasterTimer

/**
 * Basic profiler.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class Profiler {

    companion object {
        private lateinit var masterTimer: MasterTimer
        private val runtime: Runtime

        private val MB = 1024.0f * 1024.0f

        init {
            //masterTimer = FXGL.getMasterTimer()
            runtime = Runtime.getRuntime()
        }
    }

    // set to 1 to avoid div by 0
    var frames = 1
        private set

    private var fps = 0.0

    fun getAvgFPS() = fps / frames

    fun getAvgFPSRounded() = getAvgFPS().toInt()

    private var performance = 0.0

    fun getAvgPerformance() = performance / frames

    fun getAvgPerformanceRounded() = "%.3f".format(getAvgPerformance() / 1000000000.0)

    private var memoryUsage = 0L
    private var memoryUsageMin = Long.MAX_VALUE
    private var memoryUsageMax = 0L
    private var memoryUsageCurrent = 0L;

    /**
     * @return average memory usage in MB
     */
    fun getAvgMemoryUsage() = memoryUsage / frames / MB

    fun getAvgMemoryUsageRounded() = FXGLMath.roundPositive(getAvgMemoryUsage())

    /**
     * @return max (highest peak) memory usage in MB
     */
    fun getMaxMemoryUsage() = memoryUsageMax / MB

    fun getMaxMemoryUsageRounded() = FXGLMath.roundPositive(getMaxMemoryUsage())

    /**
     * @return min (lowest peak) memory usage in MB
     */
    fun getMinMemoryUsage() = memoryUsageMin / MB

    fun getMinMemoryUsageRounded() = FXGLMath.roundPositive(getMinMemoryUsage())

    /**
     * @return how much memory is used at this moment in MB
     */
    fun getCurrentMemoryUsage() = memoryUsageCurrent / MB

    fun getCurrentMemoryUsageRounded() = FXGLMath.roundPositive(getCurrentMemoryUsage())

    private var gcRuns = 0

    fun onUpdateEvent() {
        frames++
        fps += masterTimer.fps
        performance += masterTimer.performanceFPS

        val used = runtime.totalMemory() - runtime.freeMemory()

        if (used < memoryUsageCurrent) {
            gcRuns++
        }

        memoryUsageCurrent = used
        memoryUsage += memoryUsageCurrent

        if (memoryUsageCurrent > memoryUsageMax)
            memoryUsageMax = memoryUsageCurrent

        if (memoryUsageCurrent < memoryUsageMin)
            memoryUsageMin = memoryUsageCurrent
    }

    /**
     * Starts profiling FPS values.
     */
    fun start() {
        //masterTimer.addUpdateListener(this)
    }

    /**
     * Stops profiling FPS values.
     */
    fun stop() {
        //masterTimer.removeUpdateListener(this)
    }

    /**
     * Resets values to default.
     */
    fun reset() {
        frames = 1
        fps = 0.0
        performance = 0.0

        memoryUsage = 0L
        memoryUsageMin = Long.MAX_VALUE
        memoryUsageMax = 0L
        memoryUsageCurrent = 0L

        gcRuns = 0
    }

    /**
     * Print profiles values to SystemLogger.
     */
    fun print() {
        val log = FXGLLogger.get(javaClass)

        log.info("Processed Frames: $frames")
        log.info("Average FPS: ${getAvgFPSRounded()}")
        log.info("Avg Frame Took: ${getAvgPerformanceRounded()} sec")
        log.info("Avg Memory Usage: ${getAvgMemoryUsageRounded()} MB")
        log.info("Min Memory Usage: ${getMinMemoryUsageRounded()} MB")
        log.info("Max Memory Usage: ${getMaxMemoryUsageRounded()} MB")
        log.info("Estimated GC runs: $gcRuns")
    }

    // the debug data max chars is ~110, so just add a margin
    // cache string builder to avoid object allocation
    private val sb = StringBuilder(128)

    fun getInfo(): String {
        // first clear the contents
        sb.setLength(0)
        sb.append("FPS: ").append(masterTimer.fps)
                .append("\nFrame in: ").append("%.3f s".format(masterTimer.performanceFPS / 1000000000.0))
                .append("\nNow Mem (MB): ").append(getCurrentMemoryUsageRounded())
                .append("\nAvg Mem (MB): ").append(getAvgMemoryUsageRounded())
                .append("\nMin Mem (MB): ").append(getMinMemoryUsageRounded())
                .append("\nMax Mem (MB): ").append(getMaxMemoryUsageRounded())

        return sb.toString()
    }
}