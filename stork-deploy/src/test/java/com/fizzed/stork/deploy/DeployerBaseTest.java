/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.stork.deploy;

import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ContextImpl;
import static org.junit.Assume.*;
import org.junit.Before;

public class DeployerBaseTest {

    protected final String host;
    
    @Before
    public void isVagrantHostRunning() {
        assumeTrue("Is vagrant " + host + " running?", Targets.VAGRANT_CLIENT.machinesRunning().contains(host));
    }
    
    @Before
    public void bindBlazeContext() {
        // bind context before calling any blazified scripts
        ContextHolder.set(new ContextImpl(null, null, null, null));
    }
    
    public DeployerBaseTest(String host) {
        this.host = host;
    }
    
    public String getHostUri() {
        return "vagrant+ssh://" + host;
    }
    
}
