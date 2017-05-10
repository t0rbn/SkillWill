import React from 'react'
import SearchBar from './search-bar.jsx'
import Dropdown from '../dropdown/dropdown.jsx'
import SearchSuggestions from './search-suggestion/search-suggestions.jsx'
import User from '../user/user.jsx'
import getStateObjectFromURL from '../../utils/getStateObjectFromURL'
import { browserHistory } from 'react-router'
import { getUserBySearchTerms, setLocationFilter } from '../../actions'
import { connect } from 'react-redux'

class UserSearch extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			route: 'search',
			results: [],
			searchStarted: false,
			shouldUpdate: false,
		}
		this.handleDropdownSelect = this.handleDropdownSelect.bind(this)
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	handleSearchBarInput(newSearchTerms) {
		this.props.getUserBySearchTerms(newSearchTerms)
	}

	handleSearchBarDelete(deleteItem) {
		this.props.getUserBySearchTerms(deleteItem, 'delete')
	}

	handleDropdownSelect(location) {
		if (location !== "all") {
			this.props.setLocationFilter(location)
		} else {
			this.props.setLocationFilter(location)
		}
	}

	render() {
		const { results, dropdownLabel, searchItems, searchStarted } = this.state
		const { searchTerms, locationFilter } = this.props
		return (
			<div class="searchbar">
				<Dropdown
					onDropdownSelect={this.handleDropdownSelect}
					dropdownLabel={locationFilter} />
				<SearchBar
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					parent={this}
					searchTerms={searchTerms}
					noResults={results.length === 0}
					queryParams={this.props.location.query}>
					{/*<SearchSuggestions
						searchTerms={searchItems}
						noResults={results.length === 0} />*/}
				</SearchBar>
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchTerms: state.searchTerms,
		locationFilter: state.locationFilter
	}
}

export default connect(mapStateToProps, { getUserBySearchTerms, setLocationFilter })(UserSearch)
