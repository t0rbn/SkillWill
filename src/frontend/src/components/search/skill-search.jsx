import React from 'react'
import SearchBar from './search-bar.jsx'
import SkillEditor from '../skill-editor/skill-editor.jsx'
import {
	getSkillsBySearchTerm,
	getUserProfileData,
	exitSkillsEditMode,
} from '../../actions'
import { connect } from 'react-redux'

class SkillSearch extends React.Component {
	constructor(props) {
		super(props)

		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	componentWillUnmount() {
		this.props.exitSkillsEditMode()
	}

	handleSearchBarInput(newSearchTerms) {
		this.props.getSkillsBySearchTerm(newSearchTerms)
	}

	handleSearchBarDelete(deleteItem) {
		this.props.getSkillsBySearchTerm(deleteItem, 'delete')
	}

	render() {
		const { handleEdit, handleDelete, skillSearchTerms } = this.props

		return (
			<div className="skill-search">
				<div className="searchbar">
					<p className="subtitle">Add new skill</p>
					<p className="search-description">What skills do you have?</p>
					<SearchBar
						mountWithResults
						onInputChange={this.handleSearchBarInput}
						onInputDelete={this.handleSearchBarDelete}
						parent={this}
						searchTerms={skillSearchTerms}
					/>
				</div>
				<SkillEditor
					handleEdit={handleEdit}
					handleDelete={handleDelete}
					searchTerms={skillSearchTerms}
				/>
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skillSearchTerms: state.skillSearchTerms,
	}
}

export default connect(mapStateToProps, {
	getSkillsBySearchTerm,
	getUserProfileData,
	exitSkillsEditMode,
})(SkillSearch)
