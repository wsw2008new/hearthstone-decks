package com.kschmidt.hearthstone.web.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kschmidt.hearthstone.domain.Deck;
import com.kschmidt.hearthstone.domain.DeckDiff;
import com.kschmidt.hearthstone.repository.impl.IcyVeinsDeckRepository;

public class DeckDiffResource {

	@Autowired
	private IcyVeinsDeckRepository icyVeins;

	private Deck userDeck;

	@RestController
	@RequestMapping("/api")
	public class LogsResource {

		@RequestMapping(value = "/deckdiff", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
		public DeckDiff diff() throws IOException {
			Deck desiredDeck = icyVeins
					.getDeck("http://www.icy-veins.com/hearthstone/legendary-dragon-ramp-druid-brm-deck");
			return new DeckDiff(desiredDeck, userDeck);
		}

	}

}
