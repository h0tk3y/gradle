/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.operations.logging;

import org.gradle.internal.logging.buildoperation.LogEventBuildOperationProgressDetails;
import org.gradle.internal.logging.buildoperation.StyledTextBuildOperationProgressDetails;
import org.gradle.internal.logging.events.LogEvent;
import org.gradle.internal.logging.events.RenderableOutputEvent;
import org.gradle.internal.logging.events.StyledTextOutputEvent;

import java.util.List;

public final class OutputDetailsFactory {
    public static Object from(RenderableOutputEvent event) {
        if (event instanceof LogEvent) {
            final LogEvent logEvent = (LogEvent) event;
            return new LogEventBuildOperationProgressDetails() {
                @Override
                public String getMessage() {
                    return logEvent.getMessage();
                }

                @Override
                public String getCategory() {
                    return logEvent.getCategory();
                }

                /**
                 * keep log level enum here?
                 * */
                @Override
                public int getLogLevel() {
                    return logEvent.getLogLevel().ordinal();
                }

                @Override
                public Throwable getThrowable() {
                    return logEvent.getThrowable();
                }
            };

        } else if (event instanceof StyledTextOutputEvent) {
            final StyledTextOutputEvent styledTextOutputEvent = (StyledTextOutputEvent) event;

            return new StyledTextBuildOperationProgressDetails() {
                @Override
                public String getCategory() {
                    return styledTextOutputEvent.getCategory();
                }

                /**
                 * keep log level enum here?
                 * */
                @Override
                public String getLogLevel() {
                    return styledTextOutputEvent.getLogLevel().name();
                }

                @Override
                public List<StyledTextOutputEvent.Span> getSpans() {
                    return styledTextOutputEvent.getSpans();
                }
            };

        } else {
            return null;
        }
    }

    private OutputDetailsFactory() {
    }
}
