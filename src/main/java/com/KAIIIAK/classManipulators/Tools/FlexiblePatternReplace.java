package com.KAIIIAK.classManipulators.Tools;

import java.util.*;
import java.util.function.*;

public class FlexiblePatternReplace {
	
	public static class ReplaceResult<T> {
		
		public final List<T> result;
		public final boolean successfullyFully;
		public final int matchesFound;
		public final int matchesReplaced;
		
		public final List<int[]> replacedRanges;
		
		public ReplaceResult(
				List<T> result,
				boolean successfullyFully,
				int matchesFound,
				int matchesReplaced,
				List<int[]> replacedRanges
		) {
			this.result = result;
			this.successfullyFully = successfullyFully;
			this.matchesFound = matchesFound;
			this.matchesReplaced = matchesReplaced;
			this.replacedRanges = replacedRanges;
		}
		
		public String getReplacedRangesFormatedString() {
			return replacedRanges.stream()
					.map(r -> r[0] + ".." + r[1])
					.reduce((a, b) -> a + ", " + b)
					.orElse("");
		}
	}
	
	public static class MatchContext<T> {
		
		public final List<T> matchedSource = new ArrayList<>();
		public final List<T> matchedPattern = new ArrayList<>();
		public final List<Integer> matchedIndexes = new ArrayList<>();
		public final Map<String, Object> data = new HashMap<>();
		
		public <V> void put(String key, V value) {
			data.put(key, value);
		}
		
		@SuppressWarnings("unchecked")
		public <V> V get(String key) {
			return (V) data.get(key);
		}
	}
	
	public static class Match<T> {
		
		public final int start;
		public final int end;
		public final MatchContext<T> context;
		
		public Match(int start, int end, MatchContext<T> context) {
			this.start = start;
			this.end = end;
			this.context = context;
		}
	}
	
	public static <T> ReplaceResult<T> replaceMatches(
			List<T> source,
			List<T> pattern,
			Predicate<T> isIgnored,
			BiPredicate<T, T> matcher,
			BiConsumer<T, MatchContext<T>> captureSource,
			BiConsumer<T, MatchContext<T>> capturePattern,
			Function<MatchContext<T>, List<T>> replacementBuilder,
			int[] replaceOnlyMatches
	) {
		if (source.isEmpty() || pattern.isEmpty()) {
			return new ReplaceResult<>(source, false, 0, 0, new ArrayList<>());
		}
		
		boolean replaceAll = replaceOnlyMatches == null || replaceOnlyMatches.length == 0;
		
		Set<Integer> replaceSet = new HashSet<>();
		if (!replaceAll) {
			for (int idx : replaceOnlyMatches) {
				if (idx <= 0) throw new IllegalArgumentException("replaceOnlyMatches must be 1-based");
				replaceSet.add(idx);
			}
		}
		
		List<Match<T>> matches = findMatches(source, pattern, isIgnored, matcher, captureSource, capturePattern);
		
		if (matches.isEmpty()) {
			return new ReplaceResult<>(source, false, 0, 0, new ArrayList<>());
		}
		
		List<T> result = new ArrayList<>();
		int lastIndex = 0;
		
		int matchCounter = 0;
		int replacedCounter = 0;
		
		List<int[]> replacedRanges = new ArrayList<>();
		
		for (Match<T> match : matches) {
			matchCounter++;
			
			boolean shouldReplace = replaceAll || replaceSet.contains(matchCounter);
			
			// копируем до матча
			for (int i = lastIndex; i < match.start; i++) {
				result.add(source.get(i));
			}
			
			if (shouldReplace) {
				List<T> replacement = replacementBuilder.apply(match.context);
				if (replacement != null && !replacement.isEmpty()) {
					result.addAll(replacement);
				}
				replacedRanges.add(new int[]{match.start, match.end});
				replacedCounter++;
			} else {
				for (int i = match.start; i <= match.end; i++) {
					result.add(source.get(i));
				}
			}
			
			lastIndex = match.end + 1;
		}
		
		// хвост
		for (int i = lastIndex; i < source.size(); i++) {
			result.add(source.get(i));
		}
		
		boolean successfullyFully;
		
		if (replaceAll) {
			successfullyFully = replacedCounter > 0;
		} else {
			int maxRequired = Collections.max(replaceSet);
			
			successfullyFully =
					replacedCounter == replaceSet.size()
							&& matches.size() >= maxRequired;
		}
		
		return new ReplaceResult<>(result, successfullyFully, matchCounter, replacedCounter, replacedRanges);
	}
	
	private static <T> List<Match<T>> findMatches(
			List<T> source,
			List<T> pattern,
			Predicate<T> isIgnored,
			BiPredicate<T, T> matcher,
			BiConsumer<T, MatchContext<T>> captureSource,
			BiConsumer<T, MatchContext<T>> capturePattern
	) {
		List<Match<T>> matches = new ArrayList<>();
		
		int i = 0;
		while (i < source.size()) {
			Match<T> match = tryMatch(source, pattern, isIgnored, matcher, captureSource, capturePattern, i);
			
			if (match != null) {
				matches.add(match);
				i = match.end + 1; // без overlap
			} else {
				i++;
			}
		}
		
		return matches;
	}
	
	private static <T> Match<T> tryMatch(
			List<T> source,
			List<T> pattern,
			Predicate<T> isIgnored,
			BiPredicate<T, T> matcher,
			BiConsumer<T, MatchContext<T>> captureSource,
			BiConsumer<T, MatchContext<T>> capturePattern,
			int start
	) {
		MatchContext<T> ctx = new MatchContext<>();
		
		int s = start;
		int p = 0;
		
		int realStart = -1;
		int realEnd = -1;
		
		while (p < pattern.size() && isIgnored.test(pattern.get(p))) {
			p++;
		}
		if (p >= pattern.size()) return null;
		
		while (s < source.size() && p < pattern.size()) {
			
			T src = source.get(s);
			
			if (isIgnored.test(src)) {
				if (realStart != -1) realEnd = s;
				s++;
				continue;
			}
			
			while (p < pattern.size() && isIgnored.test(pattern.get(p))) {
				p++;
			}
			
			if (p >= pattern.size()) break;
			
			T pat = pattern.get(p);
			
			if (!matcher.test(src, pat)) {
				return null;
			}
			
			if (realStart == -1) realStart = s;
			realEnd = s;
			
			ctx.matchedSource.add(src);
			ctx.matchedPattern.add(pat);
			ctx.matchedIndexes.add(s);
			
			if (captureSource != null) captureSource.accept(src, ctx);
			if (capturePattern != null) capturePattern.accept(pat, ctx);
			
			s++;
			p++;
		}
		
		while (p < pattern.size() && isIgnored.test(pattern.get(p))) {
			p++;
		}
		
		if (p == pattern.size() && realStart != -1) {
			return new Match<>(realStart, realEnd, ctx);
		}
		
		return null;
	}
}
