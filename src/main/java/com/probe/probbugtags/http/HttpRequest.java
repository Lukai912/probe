/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.probe.probbugtags.http;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.URL;

/**
 * @author F43nd1r
 * @since 03.03.2017
 */
public interface HttpRequest<T> {
    void send(@NonNull URL url,String tag, @NonNull T content, CallbackListner listner) throws IOException;
}
