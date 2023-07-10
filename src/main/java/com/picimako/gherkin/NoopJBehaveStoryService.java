//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

/**
 * A no-operation implementation of the {@link JBehaveStoryService} to use when the JBehave Support plugin is not
 * installed, or installed but disabled.
 *
 * @see DefaultJBehaveStoryService
 * @since 0.2.0
 */
public class NoopJBehaveStoryService implements JBehaveStoryService {
}
