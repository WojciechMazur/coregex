/*
 * Copyright 2021 Alex Simkin
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

package com.github.simy4.coregex.junit.quickcheck;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitQuickcheck.class)
public class CoregexGeneratorTest {
  @Property
  public void shouldGenerateMatchingUUIDString(
      @Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}")
          String uuid) {
    assertEquals(uuid, UUID.fromString(uuid).toString());
  }

  @Property
  public void shouldGenerateMatchingIsoDateString(
      @Regex(
              "[12]\\d{3}-(?:0[1-9]|1[012])-(?:0[1-9]|1\\d|2[0-8])T(?:1\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d{2}[1-9])?Z")
          String iso8601Date) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    assertEquals(iso8601Date, formatter.format(formatter.parse(iso8601Date)));
  }

  @Property
  public void shouldGenerateUniqueStrings(List<@Regex("[a-zA-Z0-9]{32,}") String> strings) {
    assertTrue(
        strings.stream()
            .allMatch(
                s ->
                    s.length() >= 32
                        && s.chars().allMatch(c -> Character.isLetter(c) || Character.isDigit(c))));
    assertEquals(strings.size(), new HashSet<>(strings).size());
  }

  @Property
  public void shouldGenerateAnyString(String any) {}
}
