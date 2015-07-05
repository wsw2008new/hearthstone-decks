package com.kschmidt.hearthstone.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.kschmidt.hearthstone.domain.Card;
import com.kschmidt.hearthstone.domain.Deck;
import com.kschmidt.hearthstone.domain.DeckDiff;
import com.kschmidt.hearthstone.domain.DiffAnalyzer;
import com.kschmidt.hearthstone.repository.MongoDeckRepository;
import com.kschmidt.hearthstone.repository.impl.JSONCardRepository;

@RestController
public class DeckDiffResource {

	@Autowired
	private JSONCardRepository jsonCardRepository;

	@Autowired
	private MongoDeckRepository mongoDeckRepository;

	@Autowired
	private Deck userDeck;

	@RequestMapping(value = "/api/cards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Card> getCards() {
		return jsonCardRepository.getCards();
	}

	@RequestMapping(value = "/api/diffs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DeckDiff> diffs(
			@RequestParam(required = false, value = "collection") String collectionName,
			@RequestParam(required = false, value = "cards") List<String> cardNames)
			throws IOException {
		if (Strings.isNullOrEmpty(collectionName)) {
			collectionName = null;
		}
		if (cardNames == null) {
			cardNames = new ArrayList<String>();
		}
		List<Deck> decks = mongoDeckRepository.findByCollectionAndCards(
				collectionName, cardNames);
		return DeckDiff.diffDecks(userDeck, decks);
	}

	@RequestMapping(value = "/api/diffanalyzer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DiffAnalyzer diffAnalyzer(
			@RequestParam(required = false, value = "collection") String collectionName,
			@RequestParam(required = false, value = "cards") List<String> cardNames)
			throws IOException {
		if (Strings.isNullOrEmpty(collectionName)) {
			collectionName = null;
		}
		if (cardNames == null) {
			cardNames = new ArrayList<String>();
		}
		List<Deck> decks = mongoDeckRepository.findByCollectionAndCards(
				collectionName, cardNames);
		return new DiffAnalyzer(DeckDiff.diffDecks(userDeck, decks));
	}

}
