/* 
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.data4all.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Intent;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class GPSserviceTest {

    @Test
    public void testGPSservice() throws Exception {

        Intent startIntent = new Intent(Robolectric.application,
                GPSservice.class);
        GPSservice service = new GPSservice();

        service.onStartCommand(startIntent, 0, 0);

        assertEquals("io.github.data4all.service.GPSservice", startIntent
                .getComponent().getClassName());

    }

}
