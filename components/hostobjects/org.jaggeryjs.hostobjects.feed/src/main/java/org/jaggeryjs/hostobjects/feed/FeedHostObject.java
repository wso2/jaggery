package org.jaggeryjs.hostobjects.feed;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.i18n.iri.IRISyntaxException;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Person;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.wso2.javascript.xmlimpl.XML;

import org.jaggeryjs.hostobjects.file.*;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class FeedHostObject extends ScriptableObject {

	private static Feed feed;
	private static SyndFeed rssFeed;
	private static FeedHostObject feedHostObject;
	private static Abdera abdera;
	private static Entry entry;
	private static final String HOST_OBJECT_NAME = "Feed";
	private static boolean isRssFeed;
	private static Log log = LogFactory.getLog(FeedHostObject.class);
	public static Context ctx;

	@Override
	public String getClassName() {
		return "Feed";
	}

	/**
	 * Constructor the user will be using inside javaScript
	 */
	public static Scriptable jsConstructor(Context cx, Object[] args,
			Function ctorObj, boolean inNewExpr) throws ScriptException {
		int argsCount = args.length;

		if (argsCount > 2) {
			HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME,
					HOST_OBJECT_NAME, argsCount, true);
		}
		Abdera abdera = new Abdera();
		Factory factory = abdera.getFactory();
		feed = factory.newFeed();
		feedHostObject = new FeedHostObject();
		ctx = cx;
		if (argsCount == 0) {
			return feedHostObject;
		}
		if (argsCount == 1) {
			if (!(args[0] instanceof String)) {
				HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME,
						HOST_OBJECT_NAME, "1", "string", args[0], true);
			}

			FeedHostObject.jsFunction_getFeed(cx, null, args, null);
		}
		return feedHostObject;
	}

	public Scriptable feedConstructor() {
		Abdera abdera = new Abdera();
		Factory factory = abdera.getFactory();
		feed = factory.newFeed();
		return this;
	}

	synchronized public static void jsFunction_getFeed(Context cx,
			Scriptable thisObj, Object[] arguments, Function funObj)
			throws ScriptException {
		if (arguments.length != 1) {
			throw new ScriptException("Invalid parameter");
		}

		if (arguments[0] instanceof String) {

			feed = null;
			URL url = null;
			try {

				url = new URL((String) arguments[0]);
				feed = (Feed) Abdera.getNewParser().parse(url.openStream())
						.getRoot();
				isRssFeed = false;
			} catch (ClassCastException e) {

				XmlReader reader = null;

				try {
					reader = new XmlReader(url);
					rssFeed = new SyndFeedInput().build(reader);
					isRssFeed = true;

					for (Iterator i = rssFeed.getEntries().iterator(); i
							.hasNext();) {
						SyndEntry entry = (SyndEntry) i.next();

					}
				} catch (IOException e1) {
					throw new ScriptException(e1);
				} catch (Exception e1) {
					throw new ScriptException(e1);
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (IOException e1) {
							throw new ScriptException(e1);
						}
				}
			} catch (IRISyntaxException e) {
				throw new ScriptException(e);
			} catch (MalformedURLException e) {
				throw new ScriptException(e);
			} catch (IOException e) {
				throw new ScriptException(e);
			}

		} else {
			throw new ScriptException(
					"Invalid parameter, It is must to be a String");
		}
	}

	public void jsSet_id(Object id) {
		if (id instanceof String) {
			feed.setId((String) (id));
		} else {
			feed.setId(FOMHelper.generateUuid());
		}
	}

	public String jsGet_id() {
		if (feed != null)
			return feed.getId().toASCIIString();
		return null;
	}

	public String jsGet_author() {
		String author;
		if (isRssFeed) {
			author = rssFeed.getAuthor().toString();
		} else {
			author = feed.getAuthor().toString();
		}
		return author;
	}

	public void jsSet_author(Object author) {
		feed.addAuthor(String.valueOf(author));
	}

	public void jsSet_authors(Object author) {

		if (author instanceof String) {

			feed.addAuthor((String) (author));
		}

		if (author instanceof NativeArray) {
			NativeArray authorsPropertyArray = (NativeArray) author;
			for (Object o1 : authorsPropertyArray.getIds()) {

				int index = (Integer) o1;
				String name = authorsPropertyArray.get(index, null).toString();

				feed.addAuthor(name);
			}
		}
	}

	public NativeArray jsGet_authors() {
		String author;
		NativeArray nativeArray = new NativeArray(0);
		if (isRssFeed) {

			List list = rssFeed.getAuthors();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				String element = (String) list.get(i);
				nativeArray.put(i, nativeArray, element);
			}
			return nativeArray;
		} else {
			if (feed != null) {

				List<Person> list = feed.getAuthors();
				int size = list.size();
				for (int i = 0; i < size; i++) {
					Person element = (Person) list.get(i);
					nativeArray.put(i, nativeArray, element.getName());
				}
				return nativeArray;
			}
			return null;
		}
	}

	public void jsSet_updated(Object updated) throws ScriptException {
		Date date;

		if (updated instanceof Date) {
			date = (Date) updated;
		} else {
			date = (Date) Context.jsToJava(updated, Date.class);
		}

		if (date != null) {
			feed.setUpdated(date);
		} else {
			throw new ScriptException("Invalid parameter");
		}

	}

	public Scriptable jsGet_updated() {
		if (feed != null) {
			Scriptable js = ctx.newObject(this, "Date", new Object[] { feed.getUpdated().getTime() });
			return js;
		}
		return null;
	}

	public String jsGet_title() {
		String title;
		if (isRssFeed) {
			title = rssFeed.getTitle().toString();
		} else {
			title = feed.getTitle();
		}
		return title;
	}

	public void jsSet_title(Object title) {
		if (title instanceof XML) {
			feed.setTitleAsXhtml(title.toString());
		} else {
			feed.setTitle(String.valueOf(title));
		}
	}

	public void jsSet_rights(Object rights) {
		if (rights instanceof XML) {
			feed.setRightsAsXhtml(rights.toString());
		} else {
			feed.setRights(String.valueOf(rights));
		}
	}

	public String jsGet_rights() {
		if (feed != null)
			return feed.getRights();
		return null;
	}

	// icon processing
	public void jsSet_icon(Object iconUrl) {
		if (iconUrl instanceof String) {
			feed.setIcon(iconUrl.toString());
		}
	}

	public String jsGet_icon() {
		if (feed != null){
			if (feed.getIcon() == null) {
				return null;
			} else {
				return feed.getIcon().getPath();
			}
		}
		return null;
	}

	public void jsSet_category(Object category) {
		feed.addCategory(String.valueOf(category));
	}

	public NativeArray jsGet_category() {
		if (feed != null) {
			NativeArray nativeArray = new NativeArray(0);
			List<Category> list = feed.getCategories();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Category element = (Category) list.get(i);
				nativeArray.put(i, nativeArray,
						element.getAttributeValue("term"));
			}
			return nativeArray;
		}
		return null;
	}

	public void jsSet_contributors(Object contributor) {
		if (contributor instanceof String) {

			entry.addContributor((String) (contributor));
		}

		if (contributor instanceof NativeArray) {
			NativeArray contributorsPropertyArray = (NativeArray) contributor;
			for (Object o1 : contributorsPropertyArray.getIds()) {

				int index = (Integer) o1;
				String contributorName = contributorsPropertyArray.get(index,
						null).toString();

				feed.addContributor(contributorName);
			}
		}

	}

	public NativeArray jsGet_contributors() {
		if (feed != null) {
			NativeArray nativeArray = new NativeArray(0);
			List<Person> list = feed.getContributors();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Person element = (Person) list.get(i);
				nativeArray.put(i, nativeArray, element.getName());
			}
			return nativeArray;
		}
		return null;
	}

	public void jsSet_links(Object link) {
		if (link instanceof String) {

			feed.addLink((String) (link));
		}

		if (link instanceof NativeArray) {
			NativeArray linksPropertyArray = (NativeArray) link;
			for (Object o1 : linksPropertyArray.getIds()) {

				int index = (Integer) o1;
				String linkStr = linksPropertyArray.get(index, null).toString();

				feed.addLink(linkStr);
			}
		}

	}

	public NativeArray jsGet_links() {
		if (feed != null) {
			NativeArray nativeArray = new NativeArray(0);
			List<Link> list = feed.getLinks();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Link element = (Link) list.get(i);
				nativeArray.put(i, nativeArray, element.getHref().toString());
			}
			return nativeArray;
		}
		return null;
	}

	public String jsGet_language() {
		String language = feed.getLanguage();
		return language;
	}

	public void jsSet_language(Object language) {
		if (language instanceof String) {
			feed.setLanguage(language.toString());
		}
	}

	public String jsGet_logo() throws ScriptException {
		String logoStr = null;
		IRI logo = feed.getLogo();
		if (logo == null) {
			return null;
		}
		try {
			logoStr = logo.toURL().toString();
		} catch (MalformedURLException e) {
			throw new ScriptException(e);
		} catch (URISyntaxException e) {
			throw new ScriptException(e);
		}
		return logoStr;
	}

	public void jsSet_logo(Object logo) {
		if (logo instanceof String) {
			feed.setLogo(logo.toString());
		}
	}

	public String jsGet_alternateLink() {
		String alternateLink = feed.getAlternateLink().toString();
		return alternateLink;
	}

	/*
	 * public static EntryHostObject throws ScriptException return newAtomEntry
	 */

	public EntryHostObject[] jsGet_entries() throws ScriptException {
		Context cx = Context.getCurrentContext();
		EntryHostObject[] retEntries = null;
		List tempEntries = null;
		// Retrieving the entries from the org.jaggeryjs.hostobjects.feed1
		if (isRssFeed) {
			tempEntries = rssFeed.getEntries();
		} else {
			tempEntries = feed.getEntries();
		}

		Iterator tempEntryIterator = tempEntries.iterator();

		// Creating a list to store converted Entries
		ArrayList convertedEntries = new ArrayList();

		Entry currentEntry;

		// Converting the list of Abdera Entries to EntryHostObjects
		while (tempEntryIterator.hasNext()) {
			currentEntry = (Entry) tempEntryIterator.next();
			EntryHostObject newAtomEntry = (EntryHostObject) cx.newObject(
					feedHostObject, "Entry", new Object[0]);
			newAtomEntry.setEntry(currentEntry);
			convertedEntries.add(newAtomEntry);
		}

		retEntries = new EntryHostObject[convertedEntries.size()];
		convertedEntries.toArray(retEntries);
		return retEntries;
	}

	public void jsSet_entries(Object entryList) throws ScriptException {

		if (entryList instanceof NativeArray) {
			NativeArray fields = (NativeArray) entryList;
			for (Object o : fields.getIds()) {

				int index = (Integer) o;
				Object nativeObject = fields.get(index, null);
				addEntry(nativeObject);
			}

		} else {

			throw new ScriptException("Invalid parameter");
		}
	}

	public void addEntry(Object entryObject) throws ScriptException {

		abdera = new Abdera();
		Factory factory = abdera.getFactory();
		entry = factory.newEntry();
		if (entryObject instanceof EntryHostObject) {
			EntryHostObject entryHostObject = (EntryHostObject) entryObject;
			entry = entryHostObject.getEntry();
			feed.addEntry(entry);
		} else if (entryObject instanceof NativeObject) {

			try {
				NativeObject nativeObject = (NativeObject) entryObject;

				ScriptableObject scriptableObject = (ScriptableObject) nativeObject;

				// author and authors processing
				Object authorProperty = ScriptableObject.getProperty(
						nativeObject, "author");
				if (authorProperty instanceof String) {

					entry.addAuthor((String) (authorProperty));
				}
				Object authorsProperty = ScriptableObject.getProperty(
						nativeObject, "authors");
				if (authorsProperty instanceof NativeArray) {
					NativeArray authorsPropertyArray = (NativeArray) authorsProperty;
					for (Object o1 : authorsPropertyArray.getIds()) {

						int indexx = (Integer) o1;
						String name = authorsPropertyArray.get(indexx, null)
								.toString();

						entry.addAuthor(name);
					}

				}

				// processing category
				Object categoryProperty = ScriptableObject.getProperty(
						nativeObject, "category");
				if (categoryProperty instanceof String) {

					entry.addCategory((String) (categoryProperty));
				}
				Object categoriesProperty = ScriptableObject.getProperty(
						nativeObject, "categories");
				if (categoriesProperty instanceof NativeArray) {
					NativeArray categoriesPropertyArray = (NativeArray) categoriesProperty;
					for (Object o1 : categoriesPropertyArray.getIds()) {

						int indexC = (Integer) o1;
						String name = categoriesPropertyArray.get(indexC, null)
								.toString();

						entry.addCategory(name);
					}

				}

				// process content
				Object content = ScriptableObject.getProperty(nativeObject,
						"content");
				if (content instanceof XML) {
					entry.setContentAsXhtml(content.toString());
				} else if (content instanceof String) {
					entry.setContent(content.toString());
				} else {
					throw new ScriptException("Unsupported Content");
				}

				// process contributor
				Object contributorProperty = ScriptableObject.getProperty(
						nativeObject, "contributor");
				if (contributorProperty instanceof String) {
					entry.addContributor(contributorProperty.toString());
				}
				Object contributorsProperty = ScriptableObject.getProperty(
						nativeObject, "contributors");
				if (contributorsProperty instanceof NativeArray) {
					NativeArray contributorsPropertyArray = (NativeArray) contributorsProperty;
					for (Object o1 : contributorsPropertyArray.getIds()) {

						int index = (Integer) o1;
						String name = contributorsPropertyArray
								.get(index, null).toString();

						entry.addContributor(name);
					}
				}

				// process id
				Object idProperty = ScriptableObject.getProperty(nativeObject,
						"id");
				if (idProperty instanceof String) {
					entry.setId((String) (idProperty));
				} else {
					entry.setId(FOMHelper.generateUuid());
				}

				// process link
				// TODO link object
				Object linkProperty = ScriptableObject.getProperty(
						nativeObject, "link");
				if (linkProperty instanceof String) {
					entry.addLink((String) (linkProperty));
				}
				Object linksProperty = ScriptableObject.getProperty(
						nativeObject, "links");
				if (linksProperty instanceof NativeArray) {
					NativeArray linksPropertyArray = (NativeArray) contributorsProperty;
					for (Object o1 : linksPropertyArray.getIds()) {

						int index = (Integer) o1;
						String name = linksPropertyArray.get(index, null)
								.toString();

						entry.addLink(name);
					}
				}

				// process published
				// TODO handle javascript date
				Object publishedProperty = ScriptableObject.getProperty(
						nativeObject, "published");
				if (publishedProperty instanceof String) {
					if (publishedProperty.equals("now")) {
						entry.setPublished(new Date(System.currentTimeMillis()));
					} else {
						entry.setPublished(publishedProperty.toString());
					}
				}

				// process rights
				Object rights = ScriptableObject.getProperty(nativeObject,
						"rights");
				if (rights instanceof XML) {
					entry.setRightsAsXhtml(rights.toString());
				} else if (rights instanceof String) {
					entry.setRights(rights.toString());
				}

				// process summary
				Object summary = ScriptableObject.getProperty(nativeObject,
						"summary");
				if (summary instanceof XML) {
					entry.setSummaryAsXhtml(summary.toString());
				} else if (summary instanceof String) {
					entry.setSummary(summary.toString());
				}

				// process title
				Object title = ScriptableObject.getProperty(nativeObject,
						"title");
				if (title instanceof XML) {
					entry.setTitleAsXhtml(title.toString());
				} else if (title instanceof String) {
					entry.setTitle(title.toString());
				} else {
					throw new ScriptException("An Entry MUST have a title.");
				}

				// process updated
				Object updated = ScriptableObject.getProperty(nativeObject,
						"updated");
				if (updated instanceof String) {
					if (updated.equals("now")) {
						entry.setUpdated(new Date(System.currentTimeMillis()));
					} else {
						entry.setUpdated((String) updated);
					}
				}

			} catch (IRISyntaxException e) {
				throw new ScriptException(e);
			}
		} else if (!(entryObject instanceof EntryHostObject)) {
			throw new ScriptException("Invalid parameter");
		}
		// log.info("New Added Entry" + entry);
	}

	public String jsFunction_toString() {
		String feedString = null;
		if (isRssFeed) {
			feedString = rssFeed.toString();
		} else {

			feedString = feed.toString();
		}
		return feedString;
	}

	public Scriptable jsFunction_toXML() {
		Context cx = Context.getCurrentContext();
		if (feed != null) {
			Object[] objects = { feed };
			Scriptable xmlHostObject = cx.newObject(this, "XML", objects);
			return xmlHostObject;
		}
		return null;
	}

	public static Scriptable jsFunction_writeTo(Context cx, Scriptable thisObj,
			Object[] arguments, Function funObj) throws ScriptException {

		FeedHostObject feedObject = (FeedHostObject) thisObj;
		FileHostObject fileHostObject;
		OutputStreamWriter outputStreamWriter = null;
		try {
			if (arguments[0] instanceof String) {
				fileHostObject = (FileHostObject) cx.newObject(feedObject,
						"File", arguments);
				outputStreamWriter = new OutputStreamWriter(
						fileHostObject.getOutputStream());
				feedObject.feed.writeTo(outputStreamWriter);
				outputStreamWriter.flush();
			} else if (arguments[0] instanceof FileHostObject) {
				fileHostObject = (FileHostObject) arguments[0];
				outputStreamWriter = new OutputStreamWriter(
						fileHostObject.getOutputStream());
				feedObject.feed.writeTo(outputStreamWriter);
				outputStreamWriter.flush();
			} else {
				throw new ScriptException("Invalid parameter");
			}
			return feedObject;
		} catch (IOException e) {
			throw new ScriptException(e);
		} finally {
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.close();
				} catch (IOException e) {
					log.warn("Error closing the stream", e);
				}
			}

		}
	}

}