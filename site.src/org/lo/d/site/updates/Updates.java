package org.lo.d.site.updates;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lo.d.site.Content;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

@Component("contents.updates")
public class Updates implements Content {
	public static class UpdateInfo implements Comparable<UpdateInfo> {
		private final Version version;
		private final String fileName;

		private UpdateInfo(Version version, String fileName) {
			this.version = version;
			this.fileName = fileName;
		}

		@Override
		public int compareTo(UpdateInfo o) {
			return version.compareTo(o.version);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UpdateInfo other = (UpdateInfo) obj;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}

		public String getFileName() {
			return fileName;
		}

		public Version getVersion() {
			return version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}
	}

	public static class Version implements Comparable<Version> {
		private static class Segment implements Comparable<Segment> {
			private final Integer num;
			private final String suffix;
			private static final Pattern PETTERN_SEGMENT_FORMAT = Pattern.compile("^(?<num>[0-9]*)(?<suffix>.*)");

			private Segment(String seg) {
				Matcher matcher = PETTERN_SEGMENT_FORMAT.matcher(seg);
				if (matcher.matches()) {
					String numString = matcher.group("num");
					Integer n = null;
					if (numString != null) {
						try {
							n = Integer.parseInt(numString);
						} catch (NumberFormatException e) {
						}
					}
					num = n;
					String suf = matcher.group("suffix");
					suffix = suf == null || suf.isEmpty() ? null : suf;
				} else {
					throw new IllegalArgumentException();
				}
			}

			@Override
			public int compareTo(Segment o) {
				if (o == null) {
					return -1;
				}

				if (num != null) {
					if (o.num != null) {
						int c = num.compareTo(o.num);
						if (c != 0) {
							return c;
						}
					}
				} else {
					if (o.num != null) {
						return 1;
					}
				}

				if (suffix != null) {
					if (o.suffix != null) {
						return suffix.compareTo(o.suffix);
					} else {
						return -1;
					}
				} else {
					if (o.suffix != null) {
						return 1;
					}
				}
				return 0;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Segment other = (Segment) obj;
				if (num == null) {
					if (other.num != null)
						return false;
				} else if (!num.equals(other.num))
					return false;
				if (suffix == null) {
					if (other.suffix != null)
						return false;
				} else if (!suffix.equals(other.suffix))
					return false;
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((num == null) ? 0 : num.hashCode());
				result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
				return result;
			}

			@Override
			public String toString() {
				return "Segment [num=" + num + ", suffix=" + suffix + "]";
			}
		}

		private final List<String> segments = Lists.newArrayList();

		private Version(String value) {
			if (value == null || value.isEmpty()) {
				return;
			}
			String[] s = value.split("\\.");
			if (s.length == 0) {
				return;
			}
			for (String v : s) {
				try {
					segments.add(v);
				} catch (NumberFormatException e) {
					return;
				}
			}
		}

		@Override
		public int compareTo(Version arg0) {
			if (arg0 == null) {
				return -1;
			}
			Iterator<String> iterator = segments.iterator();
			Iterator<String> iteratorArg0 = arg0.segments.iterator();
			if (!iterator.hasNext() && iteratorArg0.hasNext()) {
				return 1;
			}

			while (iterator.hasNext()) {
				if (!iteratorArg0.hasNext()) {
					return -1;
				}
				Segment segment = new Segment(iterator.next());
				Segment segmentArg0 = new Segment(iteratorArg0.next());

				int c = segment.compareTo(segmentArg0);
				if (c != 0) {
					return c;
				}
			}

			if (iteratorArg0.hasNext()) {
				return 1;
			}

			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Version other = (Version) obj;
			if (segments == null) {
				if (other.segments != null)
					return false;
			} else if (!segments.equals(other.segments))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((segments == null) ? 0 : segments.hashCode());
			return result;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			String sep = "";
			for (String segment : segments) {
				sb.append(sep);
				sb.append(segment);
				sep = ".";
			}
			return sb.toString();
		}

	}

	private static final Pattern PETTERN_UPDATES_HTML = Pattern.compile("^(?<fileName>updates_v_(?<version>[^.]+\\.[^.]+\\.[^.]+))\\.html");

	public UpdateInfo getFirst() {
		List<UpdateInfo> updates = getUpdates();
		if (updates == null || updates.isEmpty()) {
			return null;
		}
		return updates.get(0);
	}

	public String getPath() {
		try {
			return getRootPath().relativize(Paths.get(getClass().getResource("").toURI())).toString().replaceAll("\\\\", "/");
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public List<UpdateInfo> getUpdates() {
		List<UpdateInfo> result = Lists.newArrayList();
		try {
			Path path = Paths.get(Resources.getResource(Updates.class, "").toURI());
			File dir = path.toFile();
			if (dir.exists() && dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					String fileName = file.getName();
					Matcher matcher = PETTERN_UPDATES_HTML.matcher(fileName);
					if (matcher.matches()) {
						String versionString = matcher.group("version");
						UpdateInfo updateInfo = new UpdateInfo(new Version(versionString), matcher.group("fileName"));
						result.add(updateInfo);
					}
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Collections.sort(result);
		Collections.reverse(result);
		return result;
	}

	public boolean isFirst(UpdateInfo update) {
		UpdateInfo first = getFirst();
		if (first == null) {
			return false;
		}
		return first.equals(update);
	}

	private Path getRootPath() {
		try {
			return Paths.get(getClass().getClassLoader().getResource("").toURI());
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
