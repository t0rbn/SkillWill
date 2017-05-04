import React from 'react'
import { Router, Route, Link } from 'react-router'
import SearchSuggestions from './search-suggestion/search-suggestions.jsx'
import getStateObjectFromURL from '../../utils/getStateObjectFromURL'

export default class SearchBar extends React.Component {
	constructor(props) {
		super(props)
		const { searchItems, locationString, dropdownLabel } = getStateObjectFromURL(this.props.queryParams)
		this.state = {
			searchItems,
			locationString,
			dropdownLabel,
			currentValue: '',
			searchTerms: this.props.searchTerms
		}
		this.getInputValue = this.getInputValue.bind(this)
		this.deleteFilter = this.deleteFilter.bind(this)
		this.handleSubmit = this.handleSubmit.bind(this)
		this.handleSuggestionSelected = this.handleSuggestionSelected.bind(this)
	}

	componentDidMount() {
		this.input.focus()
	}

	getInputValue(event) {
		this.setState({
			currentValue: event.target.value
		})
	}

	deleteFilter(event, deleteItem) {
		const {currentValue, searchTerms} = this.state
		const {key, type, target} = event
		const isBackspaceKey = currentValue === "" && key === 'Backspace' && searchTerms !== ""
		const isMouseClick = type === 'click' && target.dataset.filter === deleteItem

		if (isBackspaceKey || isMouseClick) {
			this.props.onInputDelete(deleteItem)
		}
	}

	handleSubmit(event) {
		event.preventDefault()
		const regex = new RegExp(/\s*,+\s*|\s+/,'g')
		const currentValue = this.state.currentValue.trim().split(regex).filter( element => element )
		if (currentValue) {
			this.props.onInputChange(currentValue)
		}
	}

	handleSuggestionSelected(name) {
		this.setState({
			searchTerms: this.state.searchTerms.concat(name),
			currentValue: ''
		})
	}

	render() {
		return (
			<div>
				<form
					onSubmit={this.handleSubmit}
					name="SearchBar"
					autocomplete="off">
					<div class="search-container">
						<div class="input-container">
							{/*display entered searchTerms in front of the input field*/}
							{this.state.searchTerms.map((searchTerm, i) => {
								return (
									<div class="search-term">
										{searchTerm}
										<a class="close" data-filter={searchTerm} key={i} onClick={event => this.deleteFilter(event, searchTerm)}>&#9747;</a>
									</div>
								)
							})}
							<input
								name="SearchInput"
								autocomplete="off"
								placeholder="Nach welchem Skill suchst du?"
								type="search"
								value={this.state.currentValue}
								autoFocus="true"
								onChange={this.getInputValue}
								onKeyDown={this.deleteFilter}
								ref={input => { this.input = input }}>
							</input>
						</div>
						<button type="submit" class="submit-search-button" />
					</div>
				</form>
				{React.cloneElement(this.props.children, { handleSuggestionSelected: this.handleSuggestionSelected, currentValue: this.state.currentValue })}
			</div>
		)
	}
}
