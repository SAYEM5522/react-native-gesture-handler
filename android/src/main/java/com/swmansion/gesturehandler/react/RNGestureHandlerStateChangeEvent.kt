package com.swmansion.gesturehandler.react

import androidx.core.util.Pools
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.swmansion.gesturehandler.GestureHandler

class RNGestureHandlerStateChangeEvent private constructor() : Event<RNGestureHandlerStateChangeEvent>() {
  private var extraData: WritableMap? = null
  private fun <T : GestureHandler<T>> init(
    handler: T,
    newState: Int,
    oldState: Int,
    dataExtractor: RNGestureHandlerEventDataExtractor<T>?,
  ) {
    super.init(handler.view!!.id)
    extraData = Arguments.createMap().apply {
      dataExtractor?.extractEventData(handler, this)
      putInt("handlerTag", handler.tag)
      putInt("state", newState)
      putInt("oldState", oldState)
    }
  }

  override fun onDispose() {
    extraData = null
    EVENTS_POOL.release(this)
  }

  override fun getEventName() = EVENT_NAME

  // TODO: coalescing
  override fun canCoalesce() = false

  // TODO: coalescing
  override fun getCoalescingKey(): Short = 0

  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    rctEventEmitter.receiveEvent(viewTag, EVENT_NAME, extraData)
  }

  companion object {
    const val EVENT_NAME = "onGestureHandlerStateChange"
    private const val TOUCH_EVENTS_POOL_SIZE = 7 // magic
    private val EVENTS_POOL = Pools.SynchronizedPool<RNGestureHandlerStateChangeEvent>(TOUCH_EVENTS_POOL_SIZE)

    @JvmStatic
    fun <T : GestureHandler<T>> obtain(
      handler: T,
      newState: Int,
      oldState: Int,
      dataExtractor: RNGestureHandlerEventDataExtractor<T>?,
    ): RNGestureHandlerStateChangeEvent =
      (EVENTS_POOL.acquire() ?: RNGestureHandlerStateChangeEvent()).apply {
        init(handler, newState, oldState, dataExtractor)
      }
  }
}
