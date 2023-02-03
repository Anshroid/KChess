package uk.co.anshroid.kchess;

import com.amazon.kindle.booklet.AbstractBooklet;
import com.amazon.kindle.booklet.BookletContext;
import com.amazon.kindle.restricted.content.catalog.ContentCatalog;
import com.amazon.kindle.restricted.runtime.Framework;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

// disable warnings for unchecked type casts
public class Util {
    private static boolean kindle;

	public static class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		public X x() {
			return x;
		}

		public Y y() {
			return y;
		}

		@Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple<?, ?> tuple = (Tuple<?, ?>) o;
            return x.equals(tuple.x) && y.equals(tuple.y);
        }
    }

    public static BookletContext obGetBookletContext(AbstractBooklet booklet){
		BookletContext bc = null;
		Method[] methods = AbstractBooklet.class.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getReturnType() == BookletContext.class) {
				// Double check that it takes no arguments, too...
                System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
				Class<?>[] params = methods[i].getParameterTypes();
                System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
				if (params.length == 0) {
					try {
                        System.err.println(i);
                        System.err.println(methods[i]);
                        System.err.println(methods[i].getReturnType().getName());
                        System.err.println(methods[i].getName());
                        System.err.println(methods[i].getParameterCount());
                        System.err.println(booklet);
                        System.err.println("---------------------------------------");
						bc = (BookletContext) methods[i].invoke(booklet, (Object[]) null);
                        System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
                        System.err.println(bc);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO: Auto-generated catch block
                        e.printStackTrace();
					}
					break;
				}
			}
		}
		System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
        return bc;
	}

	public static Container getUIContainer(AbstractBooklet booklet) throws InvocationTargetException, IllegalAccessException {

		Method getUIContainer = null;
        System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
        // Should be the only method returning a Container in BookletContext...
		Method[] methods = BookletContext.class.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getReturnType() == Container.class) {
				// Double check that it takes no arguments, too...
				Class<?>[] params = method.getParameterTypes();
				if (params.length == 0) {
					getUIContainer = method;
					System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
					break;
				}
			}
		}


		if (getUIContainer != null) {

            System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
            //new Logger().append("Found getUIContainer method as " + getUIContainer.toString());
			BookletContext bc = Util.obGetBookletContext(booklet);
            System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
            Container rootContainer = (Container) getUIContainer.invoke(bc, (Object[]) null);
            System.err.println("OOOOOOOOOOOOOOOOOOOOOOOOOOO");
            return rootContainer;
		}
		else {
			return null;
		}
	}

	// And this was always obfuscated...
	// NOTE: Pilfered from KPVBooklet (https://github.com/koreader/kpvbooklet/blob/master/src/com/github/chrox/kpvbooklet/ccadapter/CCAdapter.java)
	/**
	 * Perform CC request of type "query" and "change"
	 * @param req_type request type of "query" or "change"
	 * @param req_json request json string
	 * @return return json object
	 */
	private static JSONObject ccPerform(String req_type, String req_json) {
		ContentCatalog CC = Framework.getService(ContentCatalog.class);
		try {
			Method perform = null;

			// Enumeration approach
			Class<?>[] signature = {String.class, String.class, int.class, int.class};
			Method[] methods = ContentCatalog.class.getDeclaredMethods();
			for (Method method : methods) {
				Class<?>[] params = method.getParameterTypes();
				if (params.length == signature.length) {
					int j;
					j = 0;
					while (j < signature.length && params[j].isAssignableFrom(signature[j])) {
						j++;
					}
					if (j == signature.length) {
						perform = method;
						break;
					}
				}
			}

			if (perform != null) {
				return (JSONObject) perform.invoke(CC, new Object[] { req_type, req_json, 200, 5 });
			}
			else {
				System.err.println("Failed to find perform method, last access time won't be set on exit!");
				return new JSONObject();
			}
		} catch (Throwable t) {
			throw new RuntimeException(t.toString());
		}
	}


	public static void updateCCDB(String tag, String path) {
		long lastAccess = new Date().getTime() / 1000L;
		path = JSONObject.escape(path);
		// NOTE: Hard-code the path, as no-one should be using a custom .kual trigger...
		String json_query = "{\"filter\":{\"Equals\":{\"value\":\"" + path + "\",\"path\":\"location\"}},\"type\":\"QueryRequest\",\"maxResults\":1,\"sortOrder\":[{\"order\":\"descending\",\"path\":\"lastAccess\"},{\"order\":\"ascending\",\"path\":\"titles[0].collation\"}],\"startIndex\":0,\"id\":1,\"resultType\":\"fast\"}";
		JSONObject json = Util.ccPerform("query", json_query);
		JSONArray values = (JSONArray) json.get("values");
		JSONObject value = (JSONObject) values.get(0);
		String uuid = (String) value.get("uuid");
		String json_change = "{\"commands\":[{\"update\":{\"uuid\":\"" + uuid + "\",\"lastAccess\":" + lastAccess + ",\"displayTags\":[\"" + tag + "\"]" + "}}],\"type\":\"ChangeRequest\",\"id\":1}";
		Util.ccPerform("change", json_change);
		//new Logger().append("Set KUAL's lastAccess ccdb entry to " + lastAccess);
	}

    public static boolean isKindle() {
        return kindle;
    }

    public static void setKindle(boolean kindleValue) {
        kindle = kindleValue;
    }
}
