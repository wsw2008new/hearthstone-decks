package com.kschmidt.hearthstone.repository.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kschmidt.hearthstone.domain.Card;
import com.kschmidt.hearthstone.domain.Deck;
import com.kschmidt.hearthstone.domain.DeckCard;
import com.kschmidt.hearthstone.repository.CardRepository;
import com.kschmidt.hearthstone.repository.WebDeckRepository;

public class HearthpwnRepository implements WebDeckRepository {

	private static final String BASE_URI = "http://www.hearthpwn.com";
	private static final Logger LOG = LoggerFactory
			.getLogger(HearthpwnRepository.class);

	private CardRepository cardRepository;

	public HearthpwnRepository(CardRepository cardRepository) {
		this.cardRepository = cardRepository;
	}

	/**
	 * Return relative URLs for decks at the given location. Absolute URLs
	 * cannot be returned because Jsoup has no context.
	 */
	List<String> getDeckUrls(String deckListUrl) throws IOException {
		List<String> deckUrls = new ArrayList<String>();
		Document doc = getDocument(deckListUrl);
		// LOG.debug(doc.toString());
		Elements links = doc
				.select("table.listing-decks tbody tr td.col-name span a");
		for (Element link : links) {
			deckUrls.add(link.attr("abs:href"));
		}
		return deckUrls;
	}

	/**
	 * Hearthpwn does a cookie check, redirecting from the original url to a
	 * cookie setter to an authentication service and finally back to the
	 * original url where the cookie must be passed. Jsoup does not handle this
	 * by default so use httpclient instead and pass the result to jsoup.
	 * 
	 * @param url
	 *            the url to fetch, which should be relative to
	 *            www.hearthpwn.com
	 */
	Document getDocument(String url) throws ClientProtocolException,
			IOException {
		HttpClient client = HttpClientBuilder
				.create()
				.setDefaultRequestConfig(
						RequestConfig.custom()
								.setCircularRedirectsAllowed(true).build())
				.build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		Document doc = Jsoup.parse(result.toString());
		doc.setBaseUri(BASE_URI);
		return doc;
	}

	@Override
	public Deck getDeck(String url) throws IOException {
		Document doc = getDocument(url);
		String deckTitle = doc
				.select("header.deck-detail section.deck-info h2.deck-title")
				.get(0).text();
		Deck deck = new Deck(deckTitle);
		deck.setUrl(url);

		Elements cardElements = doc
				.select("aside.infobox table.listing-cards-tabular tr td.col-name");
		for (Element cardElement : cardElements) {
			Pattern p = Pattern.compile("(.*)× (\\d)$");
			Matcher m = p.matcher(cardElement.text());
			if (m.matches()) {
				Card card = cardRepository.findCard(m.group(1));
				DeckCard deckCard = new DeckCard(card, Integer.parseInt(m
						.group(2)));
				deck.add(deckCard);
			} else {
				throw new IllegalStateException("Text: '" + cardElement.text()
						+ "' did not parse as a card");
			}
		}
		return deck;
	}

	@Override
	public List<Deck> getDecks(String url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Deck> getAllDecks() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}