package org.wisp.gatesawakened.jumping

/**
 * Taken from <a href="https://github.com/mattdesl/cisc226game/blob/master/SpaceGame/src/space/engine/easing/Easing.java">Github</a>
 *
 * @author Robert Penner (functions)
 * @author davedes (java port)
 * @author Wisp (kotlin port)
 */
object Easing {
    object Quadratic {
        /**
         * Quadratic easing in - accelerating from zero velocity.
         */
        fun easeIn(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return valueAtEnd * (run { t /= duration;t }) * t + valueAtStart
        }

        /**
         * Quadratic easing out - decelerating to zero velocity.
         */
        fun easeOut(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return -valueAtEnd * (run { t /= duration;t }) * (t - 2) + valueAtStart
        }

        /**
         * Quadratic easing in/out - acceleration until halfway, then deceleration
         */
        fun easeInThenOut(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return if ((run { t /= duration / 2;t }) < 1)
                valueAtEnd / 2 * t * t + valueAtStart;
            else -valueAtEnd / 2 * ((--t) * (t - 2) - 1) + valueAtStart;
        }
    }

    object Linear {
        /**
         * Simple linear tweening - no easing.
         */
        fun tween(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float =
            valueAtEnd * time / duration + valueAtStart
    }

    object Cubic {
        /**
         * Cubic easing in - accelerating from zero velocity.
         */
        fun easeIn(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return valueAtEnd * (run { t /= duration;t }) * t * t + valueAtStart
        }

        /**
         * Cubic easing out - decelerating to zero velocity.
         */
        fun easeOut(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return valueAtEnd * ((run { t = t / duration - 1;t }) * t * t + 1) + valueAtStart;
        }

        /**
         * Cubic easing in/out - acceleration until halfway, then deceleration
         */
        fun easeInThenOut(time: Float, valueAtStart: Float, valueAtEnd: Float, duration: Float): Float {
            var t = time
            return if ((run { t /= duration / 2;t }) < 1)
                valueAtEnd / 2 * t * t * t + valueAtStart
            else valueAtEnd / 2 * ((run { t -= 2;t }) * t * t + 2) + valueAtStart;
        }
    }
}