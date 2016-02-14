import React, { Component } from 'react';

export default class DiffSearchCriteria extends Component {
	render() {
		return (
			<section className="panel">
				<div className="panel-body">
					<form className="form-horizontal">
						<div className="form-group">
							<label className="col-sm-2 control-label">Collection</label>
							<div className="col-sm-10">
								<select className="form-control">
									<option value="">All</option>
									<option value="hearthpwnRepository">Hearthpwn</option>
									<option value="hearthstoneTopDeckRepository">Hearthstone
										TopDecks</option>
									<option value="icyVeinsDeckRepository">IcyVeins</option>

									<option value="tempoStormDeckRepository">Tempo Storm</option>
								</select>
							</div>
						</div>
					</form>
				</div>
			</section>
		);
	}
}