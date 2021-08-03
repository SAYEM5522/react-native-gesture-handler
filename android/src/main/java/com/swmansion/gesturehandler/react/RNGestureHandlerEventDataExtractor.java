package com.swmansion.gesturehandler.react;

import com.facebook.react.bridge.WritableMap;
import com.swmansion.gesturehandler.GestureHandler;

public interface RNGestureHandlerEventDataExtractor<T extends GestureHandler<T>> {
  void extractEventData(T handler, WritableMap eventData);
}
