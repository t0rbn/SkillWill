import React from 'react'
import { connect } from 'react-redux'
import Icon from '../icon/icon.jsx'
import SearchSuggestions from './search-suggestion/search-suggestions.jsx'

export class SearchBar extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			searchTerms: [],
			currentValue: '',
		}
		this.getInputValue = this.getInputValue.bind(this)
		this.deleteFilter = this.deleteFilter.bind(this)
		this.handleSubmit = this.handleSubmit.bind(this)
		this.handleSuggestionSelected = this.handleSuggestionSelected.bind(this)
	}

	componentDidMount() {
		this.input.focus()
		this.props.mountWithResults && this.handleSubmit()
	}

	getInputValue(event) {
		this.setState({
			currentValue: event.target.value,
		})
	}

	deleteFilter(event, deleteItem) {
		const { currentValue, searchTerms } = this.state
		const { key, type, currentTarget } = event
		const isBackspaceKey =
			currentValue === '' && key === 'Backspace' && searchTerms !== ''
		const isMouseClick =
			type === 'click' && currentTarget.dataset.filter === deleteItem

		if (isBackspaceKey || isMouseClick) {
			this.props.onInputDelete(deleteItem)
			// remove item from search array
			const searchSet = new Set(searchTerms)
			searchSet.delete(deleteItem)
			this.setState({ searchTerms: [...searchSet] })
		}
	}

	handleSubmit(event) {
		event && event.preventDefault()
		const regex = new RegExp(/\s*,+\s*/, 'g')
		const filteredCurrentValue = this.state.currentValue
			.trim()
			.split(regex)
			.filter(element => element)
		if (filteredCurrentValue.length > 0 || this.props.mountWithResults) {
			this.props.onInputChange(filteredCurrentValue)
		}
		this.setState({
			currentValue: '',
		})
	}

	handleSuggestionSelected(suggestion) {
		const { searchTerms } = this.props

		if (!searchTerms.includes(suggestion)) {
			this.setState(
				prevState => {
					return {
						searchTerms: [...prevState.searchTerms, suggestion],
						currentValue: suggestion,
					}
				},
				() => this.handleSubmit()
			)
		} else {
			this.setState({ currentValue: '' })
		}
		this.input.focus()
	}

	render() {
		const { searchTerms } = this.props
		return (
			<div>
				<form onSubmit={this.handleSubmit} name="SearchBar" autoComplete="off">
					<div className="search-container">
						<div className="input-container">
							{searchTerms.map(searchTerm => {
								return (
									<div className="search-term" key={searchTerm}>
										{searchTerm}
										<a
											className="close"
											data-filter={searchTerm}
											key={`delete_${searchTerm}`}
											onClick={event => this.deleteFilter(event, searchTerm)}>
											<Icon name="cross" size={12} />
										</a>
									</div>
								)
							})}
							<input
								name="SearchInput"
								autoComplete="off"
								required="true"
								spellCheck="false"
								placeholder="Search for skills"
								type="search"
								value={this.state.currentValue}
								autoFocus="true"
								onChange={this.getInputValue}
								onKeyDown={event =>
									this.deleteFilter(event, searchTerms.slice(-1)[0])}
								ref={input => {
									this.input = input
								}}
							/>
						</div>
						<button
							type="submit"
							className={`submit-search-button ${this.state.currentValue === ''
								? 'submit-search-button--faded'
								: ''}`}>
							<Icon name="search" size={24} />
						</button>
					</div>
					<SearchSuggestions
						variant={this.props.variant}
						searchTerms={this.state.searchTerms}
						currentValue={this.state.currentValue}
						handleSuggestionSelected={this.handleSuggestionSelected}
					/>
				</form>
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skillSearchTerms: state.searchTerms,
	}
}

export default connect(mapStateToProps)(SearchBar)
