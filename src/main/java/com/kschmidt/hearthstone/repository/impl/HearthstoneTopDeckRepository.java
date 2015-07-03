package com.kschmidt.hearthstone.repository.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import com.kschmidt.hearthstone.domain.Card;
import com.kschmidt.hearthstone.domain.Deck;
import com.kschmidt.hearthstone.domain.DeckCard;
import com.kschmidt.hearthstone.repository.CardRepository;
import com.kschmidt.hearthstone.repository.WebDeckRepository;

public class HearthstoneTopDeckRepository implements WebDeckRepository {

	private static final Logger LOG = LoggerFactory
			.getLogger(HearthstoneTopDeckRepository.class);

	private CardRepository cardRepository;

	public HearthstoneTopDeckRepository(CardRepository cardRepository) {
		this.cardRepository = cardRepository;
	}

	public Deck getDeck(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		String deckName = doc.select("div#center div.headbar div").first()
				.text();
		Deck deck = new Deck(deckName);
		deck.setUrl(url);
		Element deckCardListTable = doc.select(
				"div#contentfr table :has(div.cardname)").get(0);
		Elements cardElements = deckCardListTable.select("div.cardname span");
		Pattern p = Pattern.compile("(\\d) (.*?)");
		for (int i = 0; i < cardElements.size(); ++i) {
			Element cardElement = cardElements.get(i);
			Matcher m = p.matcher(cardElement.text());
			if (m.matches()) {
				Card card = cardRepository.findCard(m.group(2));
				DeckCard deckCard = new DeckCard(card, Integer.parseInt(m
						.group(1)));
				deck.add(deckCard);
			} else {
				throw new IllegalStateException("Text: '" + cardElement.text()
						+ "' did not parse as a card");
			}
		}
		if (deck.getNumCards() != 30) {
			throw new IllegalArgumentException("Did not find 30 cards at: "
					+ url);
		}
		return deck;
	}

	public List<Deck> getAllDecks() throws IOException {
		List<Deck> decks = new ArrayList<Deck>();
		String deckListUrlPrefix = "http://www.hearthstonetopdeck.com/metagame.php?m=";
		String deckListUrlPostfix = "&t=0&filter=current";
		String[] classes = new String[] { "Druid", "Hunter", "Mage", "Paladin",
				"Priest", "Rogue", "Shaman", "Warlock", "Warrior" };
		for (int i = 0; i < classes.length; ++i) {
			String deckListUrl = deckListUrlPrefix + classes[i]
					+ deckListUrlPostfix;
			LOG.debug("Fetching decks from: " + deckListUrl);
			decks.addAll(getDecks(deckListUrl));
		}
		return decks;
	}

	@Cacheable("decks")
	public List<Deck> getDecks(String deckListUrl) throws IOException {
		List<Deck> decks = new ArrayList<Deck>();
		for (String deckUrl : getDeckUrls(deckListUrl)) {
			decks.add(getDeck(deckUrl));
		}
		if (decks.isEmpty()) {
			throw new IllegalArgumentException("No decks found at: "
					+ deckListUrl);
		}
		return decks;
	}

	List<String> getDeckUrls(String deckListUrl) throws IOException {
		List<String> urls = new ArrayList<String>();
		Document doc = Jsoup.connect(deckListUrl).get();
		Elements deckRows = doc
				.select("div#contentfr table tr:has(td.tdstyle1,td.tdstyle2)");
		for (Element deckRow : deckRows) {
			Element link = deckRow.select("a").first();
			urls.add(link.attr("abs:href"));
		}
		return urls;
	}

}