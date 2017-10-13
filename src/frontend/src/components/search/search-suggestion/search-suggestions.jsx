import React from 'react'
import { apiServer } from '../../../env.js'
import SuggestionItem from './suggestion-item'

export default class SearchSuggestions extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			results: [],
			doAutoComplete: true,
		}
		this.getAutocompletion = this.getAutocompletion.bind(this)
		this.getHint = this.getHint.bind(this)
	}

	componentWillReceiveProps() {
		setTimeout(() => {
			this.getAutocompletion(this.props.currentValue)
		}, 500)
	}

	getAutocompletion(searchTerm) {
		searchTerm !== '' &&
			fetch(
				`${apiServer}/skills?count=5&search=${encodeURIComponent(searchTerm)}`,
				{
					credentials: 'same-origin',
				}
			)
				.then(res => (res.status === 200 ? res.json() : []))
				.then(data => this.setState({ results: data }))
				.catch(err => console.log(err))
	}

	getHint() {
		if (this.state.doAutoComplete) {
			return 'Matching Skills:'
		}
		if (!this.props.noResults && this.props.searchTerms.length > 0) {
			return 'Relevant Skills:'
		}
		return 'Popular Skills:'
	}

	render() {
		const { currentValue, handleSuggestionSelected, variant } = this.props
		const { results } = this.state
		return (
			<div
				className={
					results.length > 0 && currentValue
						? `search-suggestions search-suggestions--${variant}`
						: `search-suggestions search-suggestions--${variant} hidden`
				}>
				<span className="search-suggestions__info">{this.getHint()}</span>
				<ul className="search-suggestions-list">
					{results.map((suggestion, i) => (
						<SuggestionItem
							name={suggestion.name}
							handleSuggestionSelected={handleSuggestionSelected}
							key={i}
						/>
					))}
				</ul>
			</div>
		)
	}
}
