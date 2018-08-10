/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.util.unit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A data size, such as '12MB'.
 * <p>
 * This class models a size in terms of bytes and is immutable and thread-safe.
 *
 * @author Stephane Nicoll
 * @since 5.1
 */
public final class DataSize implements Comparable<DataSize> {

	/**
	 * The pattern for parsing.
	 */
	private static final Pattern PATTERN = Pattern.compile("^([+\\-]?\\d+)([a-zA-Z]{0,2})$");

	/**
	 * Bytes per KiloByte.
	 */
	private static long BYTES_PER_KB = 1024;

	/**
	 * Bytes per MegaByte.
	 */
	private static long BYTES_PER_MB = BYTES_PER_KB * 1024;

	/**
	 * Bytes per GigaByte.
	 */
	private static long BYTES_PER_GB = BYTES_PER_MB * 1024;

	/**
	 * Bytes per TeraByte.
	 */
	private static long BYTES_PER_TB = BYTES_PER_GB * 1024;


	private final long bytes;

	private DataSize(long bytes) {
		this.bytes = bytes;
	}


	/**
	 * Obtain a {@link DataSize} representing the specified number of bytes.
	 * @param bytes the number of bytes, positive or negative
	 * @return a {@link DataSize}
	 */
	public static DataSize ofBytes(long bytes) {
		return new DataSize(bytes);
	}

	/**
	 * Obtain a {@link DataSize} representing the specified number of kilobytes.
	 * @param kiloBytes the number of kilobytes, positive or negative
	 * @return a {@link DataSize}
	 */
	public static DataSize ofKiloBytes(long kiloBytes) {
		return new DataSize(Math.multiplyExact(kiloBytes, BYTES_PER_KB));
	}

	/**
	 * Obtain a {@link DataSize} representing the specified number of megabytes.
	 * @param megaBytes the number of megabytes, positive or negative
	 * @return a {@link DataSize}
	 */
	public static DataSize ofMegaBytes(long megaBytes) {
		return new DataSize(Math.multiplyExact(megaBytes, BYTES_PER_MB));
	}

	/**
	 * Obtain a {@link DataSize} representing the specified number of gigabytes.
	 * @param gigaBytes the number of gigabytes, positive or negative
	 * @return a {@link DataSize}
	 */
	public static DataSize ofGigaBytes(long gigaBytes) {
		return new DataSize(Math.multiplyExact(gigaBytes, BYTES_PER_GB));
	}

	/**
	 * Obtain a {@link DataSize} representing the specified number of terabytes.
	 * @param teraBytes the number of terabytes, positive or negative
	 * @return a {@link DataSize}
	 */
	public static DataSize ofTeraBytes(long teraBytes) {
		return new DataSize(Math.multiplyExact(teraBytes, BYTES_PER_TB));
	}

	/**
	 * Obtain a {@link DataSize} representing an amount in the specified {@link DataUnit}.
	 * @param amount the amount of the size, measured in terms of the unit, positive or
	 * negative
	 * @return a {@link DataSize}
	 */
	public static DataSize of(long amount, DataUnit unit) {
		Assert.notNull(unit, "Unit must not be null");
		return new DataSize(Math.multiplyExact(amount, unit.getSize().toBytes()));
	}

	/**
	 * Obtain a {@link DataSize} from a text string such as {@code 12MB} using
	 * {@link DataUnit#BYTES} if no unit is specified.
	 * <p>
	 * Examples:
	 * <pre>
	 * "12KB" -- parses as "12 kilobytes"
	 * "5MB"  -- parses as "5 megabytes" (where a minute is 60 seconds)
	 * "20"   -- parses as "20 bytes"
	 * </pre>
	 * @param text the text to parse
	 * @return the parsed {@link DataSize}
	 * @see #parse(CharSequence, DataUnit)
	 */
	public static DataSize parse(CharSequence text) {
		return parse(text, null);
	}

	/**
	 * Obtain a {@link DataSize} from a text string such as {@code 12MB} using
	 * the specified default {@link DataUnit} if no unit is specified.
	 * <p>
	 * The string starts with a number followed optionally by a unit matching one of the
	 * supported {@link DataUnit suffixes}.
	 * <p>
	 * Examples:
	 * <pre>
	 * "12KB" -- parses as "12 kilobytes"
	 * "5MB"  -- parses as "5 megabytes" (where a minute is 60 seconds)
	 * "20"   -- parses as "20 kilobytes" (where the {@code defaultUnit} is {@link DataUnit#KILOBYTES})
	 * </pre>
	 * @param text the text to parse
	 * @return the parsed {@link DataSize}
	 */
	public static DataSize parse(CharSequence text, @Nullable DataUnit defaultUnit) {
		Assert.notNull(text, "Text must not be null");
		try {
			Matcher matcher = PATTERN.matcher(text);
			Assert.state(matcher.matches(), "Does not match data size pattern");
			DataUnit unit = determineDataUnit(matcher.group(2), defaultUnit);
			Long amount = Long.parseLong(matcher.group(1));
			return DataSize.of(amount, unit);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(
					"'" + text + "' is not a valid data size", ex);
		}
	}

	private static DataUnit determineDataUnit(String suffix,
			@Nullable DataUnit defaultUnit) {
		defaultUnit = (defaultUnit != null ? defaultUnit : DataUnit.BYTES);
		return (StringUtils.hasLength(suffix) ? DataUnit.fromSuffix(suffix)
				: defaultUnit);
	}

	/**
	 * Checks if this size is negative, excluding zero.
	 * @return true if this size has a size less than zero bytes
	 */
	public boolean isNegative() {
		return this.bytes < 0;
	}

	/**
	 * Return the number of bytes in this instance.
	 * @return the number of bytes
	 */
	public long toBytes() {
		return this.bytes;
	}

	/**
	 * Return the number of kilobytes in this instance.
	 * @return the number of kilobytes
	 */
	public long toKiloBytes() {
		return this.bytes / BYTES_PER_KB;
	}

	/**
	 * Return the number of megabytes in this instance.
	 * @return the number of megabytes
	 */
	public long toMegaBytes() {
		return this.bytes / BYTES_PER_MB;
	}

	/**
	 * Return the number of gigabytes in this instance.
	 * @return the number of gigabytes
	 */
	public long toGigaBytes() {
		return this.bytes / BYTES_PER_GB;
	}

	/**
	 * Return the number of terabytes in this instance.
	 * @return the number of terabytes
	 */
	public long toTeraBytes() {
		return this.bytes / BYTES_PER_TB;
	}

	@Override
	public int compareTo(DataSize other) {
		return Long.compare(this.bytes, other.bytes);
	}

	@Override
	public String toString() {
		return String.format("%dB", this.bytes);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DataSize that = (DataSize) o;
		return this.bytes == that.bytes;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.bytes);
	}

}
