package com.kschmidt.hearthstone.repository.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kschmidt.hearthstone.domain.Card;
import com.kschmidt.hearthstone.domain.Deck;
import com.kschmidt.hearthstone.domain.DeckCard;
import com.kschmidt.hearthstone.repository.CardRepository;

public class HearthstoneTopDeckRepository extends AbstractWebDeckRepository {

	private static final List<String> DECK_BLACKLIST = Arrays.asList(new String[] {});
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(HearthstoneTopDeckRepository.class);
	private static final int TIMEOUT_MILLIS = 10000;

	private CardRepository cardRepository;

	public HearthstoneTopDeckRepository(CardRepository cardRepository) {
		this.cardRepository = cardRepository;
	}

	public Deck getDeck(String url) throws IOException {

		Connection conn = Jsoup.connect(url);
		conn.timeout(TIMEOUT_MILLIS);
		Document doc = conn.get();

		Deck deck = new Deck(getDeckName(doc));
		deck.setCollection("hearthstoneTopDeckRepository");
		deck.setUrl(url);
		deck.setLastUpdated(getLastUpdated(doc, url));

		// Element deckCardListTable = doc.select(
		// "div#contentfr table :has(div.cardname)").get(0);
		Elements cardElements = doc.select("div.cardname span");
		Pattern p = Pattern.compile("(\\d) (.*?)");
		for (int i = 0; i < cardElements.size(); ++i) {
			Element cardElement = cardElements.get(i);
			Matcher m = p.matcher(cardElement.text());
			if (m.matches()) {
				Card card = cardRepository.findCard(m.group(2));
				DeckCard deckCard = new DeckCard(card, Integer.parseInt(m.group(1)));
				deck.add(deckCard);
			} else {
				throw new IllegalStateException("Text: '" + cardElement.text() + "' did not parse as a card");
			}
		}
		if (deck.getNumCards() != 30) {
			throw new IllegalArgumentException("Did not find 30 cards at: " + url);
		}
		return deck;
	}

	private LocalDate getLastUpdated(Document doc, String url) {
		String selector = ".deck_banner_description .small";
		Elements elements = doc.select(selector);
		Preconditions.checkState(elements.size() != 0,
				"No elements found for selector: " + selector + " in document: " + doc.baseUri());
		String text = elements.get(0).text();
		return parseLastUpdated(text);
	}

	private String getDeckName(Document doc) {
		final String selector = ".panel-title";
		Elements elements = doc.select(selector);
		Preconditions.checkState(elements.size() != 0,
				"No elements found for selector: " + selector + " in document: " + doc.baseUri());
		String name = elements.get(0).text();
		Preconditions.checkState(!Strings.isNullOrEmpty(name),
				"selector: " + selector + " has no text in document: " + doc.baseUri());
		return name;
	}

	public List<Deck> getAllDecks() throws IOException {
		List<Deck> decks = new ArrayList<Deck>();
		String deckListUrlPrefix = "http://www.hearthstonetopdeck.com/metagame/standard/";
		String deckListUrlPostfix = "/0";
		String[] classes = new String[] { "druid", "hunter", "mage", "paladin", "priest", "rogue", "shaman", "warlock",
				"warrior" };
		for (int i = 0; i < classes.length; ++i) {
			String deckListUrl = deckListUrlPrefix + classes[i] + deckListUrlPostfix;
			decks.addAll(getDecks(deckListUrl));
		}
		return decks;
	}

	public List<String> getDeckUrls(String deckListUrl) throws IOException {
		List<String> urls = new ArrayList<String>();
		Document doc = Jsoup.connect(deckListUrl).get();
		Elements deckRows = doc.select("div.panel-body table tr:has(td.tdstyle1,td.tdstyle2)");
		for (Element deckRow : deckRows) {
			Element link = deckRow.select("a").first();
			urls.add(link.attr("abs:href"));
		}
		urls.removeAll(DECK_BLACKLIST);
		return urls;
	}

	public String toString() {
		return "HearthstoneTopDeckRepository";
	}

	LocalDate parseLastUpdated(String text) {
		String pattern = "Format: (.*) - Updated:.([\\d/]*)";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		if (m.matches()) {
			String dateString = m.group(2).trim();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu/MM/d");
			LocalDate date = LocalDate.parse(dateString, formatter);
			return date;
		} else {
			throw new IllegalStateException("Text: " + text + " does not match pattern: '" + pattern + "'");
		}
	}

}
