import React from 'react'
import SearchBar from './search-bar.jsx'
import {getUserBySearchTerms} from '../../actions'
import {connect} from 'react-redux'

class UserSearch extends React.Component {
	constructor(props) {
		super(props)
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	handleSearchBarInput(newSearchTerms) {
		this.props.getUserBySearchTerms(newSearchTerms)
	}

	handleSearchBarDelete(deleteItem) {
		this.props.getUserBySearchTerms(deleteItem, 'delete')
	}

	render() {
		const { searchTerms } = this.props;
		return (
			<div className="searchbar">
				<SearchBar
					variant="user"
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					searchTerms={searchTerms}
				/>
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchTerms: state.searchTerms
	}
}

export default connect(mapStateToProps, {
	getUserBySearchTerms,
})(UserSearch)
