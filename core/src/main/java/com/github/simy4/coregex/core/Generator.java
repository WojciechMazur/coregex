/*
 * Copyright 2021-2023 Alex Simkin
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

package com.github.simy4.coregex.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

final class Generator implements Coregex.Visitor<Optional<String>> {
  private RNG rng;
  private int remainder;

  Generator(RNG rng, int remainder) {
    this.rng = rng;
    this.remainder = remainder;
  }

  @Override
  public Optional<String> visit(Coregex.Concat c) {
    int minLength = c.minLength();
    StringBuilder sb = new StringBuilder(minLength + 16);
    remainder -= minLength;
    Iterator<Coregex> concat = c.concat().iterator();
    while (concat.hasNext() && null != sb) {
      Coregex chunk = concat.next();
      remainder += chunk.minLength();
      Optional<String> sampled = chunk.visit(this);
      if (sampled.isPresent()) {
        sb.append(sampled.get());
      } else {
        sb = null;
      }
    }
    return null == sb ? Optional.empty() : Optional.of(sb.toString());
  }

  @Override
  public Optional<String> visit(Coregex.Literal c) {
    String literal = c.literal();
    if (0 != (c.flags() & Pattern.CASE_INSENSITIVE)) {
      StringBuilder sb = new StringBuilder(literal);
      for (int i = 0; i < sb.length(); i++) {
        char ch = sb.charAt(i);
        if (Character.isLowerCase(ch) && genBoolean()) {
          sb.setCharAt(i, Character.toUpperCase(ch));
        }
        if (Character.isUpperCase(ch) && genBoolean()) {
          sb.setCharAt(i, Character.toLowerCase(ch));
        }
      }
      literal = sb.toString();
    } else {
      genBoolean(); // need to burn one random number to make result deterministic
    }
    remainder -= literal.length();
    return Optional.of(literal);
  }

  @Override
  public Optional<String> visit(Coregex.Quantified c) {
    Optional<String> sampled;
    Coregex quantified = c.quantified();
    int minLength = c.minLength(),
        min = c.min(),
        max = c.max(),
        quantifiedMinLength = quantified.minLength();
    StringBuilder sb = new StringBuilder(minLength + 16);
    int quantifier = 0;
    remainder -= minLength;
    for (; quantifier < min && null != sb; quantifier++) {
      remainder += quantifiedMinLength;
      sampled = quantified.visit(this);
      if (sampled.isPresent()) {
        sb.append(sampled.get());
      } else {
        sb = null;
      }
    }
    while (null != sb
        && quantifiedMinLength <= remainder
        && (-1 == max || quantifier++ < max)
        && genBoolean()) {
      sampled = quantified.visit(this);
      if (sampled.isPresent()) {
        sb.append(sampled.get());
      } else {
        sb = null;
      }
    }
    return null == sb ? Optional.empty() : Optional.of(sb.toString());
  }

  @Override
  public Optional<String> visit(Coregex.Set c) {
    OptionalInt sampled = c.set().apply(genLong());
    if (sampled.isPresent()) {
      remainder -= 1;
      return Optional.of(String.valueOf((char) sampled.getAsInt()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> visit(Coregex.Sized c) {
    return c.sized().visit(this);
  }

  @Override
  public Optional<String> visit(Coregex.Union c) {
    List<Coregex> union = c.union(), fits = new ArrayList<>(union.size());
    for (Coregex coregex : union) {
      if (coregex.minLength() <= remainder) {
        fits.add(coregex);
      }
    }
    if (fits.isEmpty()) {
      return Optional.empty();
    }

    return fits.get(genInteger(fits.size())).visit(this);
  }

  private boolean genBoolean() {
    Pair<RNG, Boolean> rngAndBoolean = rng.genBoolean();
    rng = rngAndBoolean.getFirst();
    return rngAndBoolean.getSecond();
  }

  private int genInteger(int bound) {
    Pair<RNG, Integer> rngAndInt = rng.genInteger(bound);
    rng = rngAndInt.getFirst();
    return rngAndInt.getSecond();
  }

  private long genLong() {
    Pair<RNG, Long> rngAndLong = rng.genLong();
    rng = rngAndLong.getFirst();
    return rngAndLong.getSecond();
  }
}
