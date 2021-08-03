package com.swmansion.gesturehandler.react

import androidx.core.util.Pools
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.swmansion.gesturehandler.GestureHandler

class RNGestureHandlerEvent private constructor() : Event<RNGestureHandlerEvent>() {
  private var extraData: WritableMap? = null
  private var coalescingKey: Short = 0
  private fun <T : GestureHandler<T>> init(
    handler: T,
    dataExtractor: RNGestureHandlerEventDataExtractor<T>?,
  ) {
    super.init(handler.view!!.id)
    extraData = Arguments.createMap()
    dataExtractor?.extractEventData(handler, extraData)
    extraData!!.putInt("handlerTag", handler.tag)
    extraData!!.putInt("state", handler.state)
    coalescingKey = handler.eventCoalescingKey
  }

  override fun onDispose() {
    extraData = null
    EVENTS_POOL.release(this)
  }

  override fun getEventName() = EVENT_NAME

  override fun canCoalesce() = true

  override fun getCoalescingKey() = coalescingKey

  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    rctEventEmitter.receiveEvent(viewTag, EVENT_NAME, extraData)
  }

  companion object {
    const val EVENT_NAME = "onGestureHandlerEvent"
    private const val TOUCH_EVENTS_POOL_SIZE = 7 // magic
    private val EVENTS_POOL = Pools.SynchronizedPool<RNGestureHandlerEvent>(TOUCH_EVENTS_POOL_SIZE)

    @JvmStatic
    fun <T : GestureHandler<T>> obtain(
      handler: T,
      dataExtractor: RNGestureHandlerEventDataExtractor<T>?,
    ): RNGestureHandlerEvent {
      var event = EVENTS_POOL.acquire()
      if (event == null) {
        event = RNGestureHandlerEvent()
      }
      event.init(handler, dataExtractor)
      return event
    }
  }
}
