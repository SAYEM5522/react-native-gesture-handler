package com.swmansion.gesturehandler.react

import androidx.core.util.Pools
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.swmansion.gesturehandler.GestureHandler

class RNGestureHandlerStateChangeEvent private constructor() : Event<RNGestureHandlerStateChangeEvent>() {
  private var mExtraData: WritableMap? = null
  private fun <T : GestureHandler<T>> init(
    handler: T,
    newState: Int,
    oldState: Int,
    dataExtractor: RNGestureHandlerEventDataExtractor<T>?,
  ) {
    super.init(handler.view!!.id)
    mExtraData = Arguments.createMap().apply {
      dataExtractor?.extractEventData(handler, this)
      putInt("handlerTag", handler.tag)
      putInt("state", newState)
      putInt("oldState", oldState)
    }
  }

  override fun onDispose() {
    mExtraData = null
    EVENTS_POOL.release(this)
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }

  override fun canCoalesce(): Boolean {
    // TODO: coalescing
    return false
  }

  override fun getCoalescingKey(): Short {
    // TODO: coalescing
    return 0
  }

  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    rctEventEmitter.receiveEvent(viewTag, EVENT_NAME, mExtraData)
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
