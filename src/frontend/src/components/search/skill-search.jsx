import React from 'react'
import SearchBar from './search-bar.jsx'
import Results from '../results/results.jsx'
import SkillEditor from '../skill-editor/skill-editor.jsx'
import config from '../../config.json'
import { getSkillsBySearchTerm, getUserProfileData } from '../../actions'
import { connect } from 'react-redux'

class SkillSearch extends React.Component {

	constructor(props) {
		super(props)
		this.state = {
			results: null,
			searchItems: [],
			searchStarted: false,
			shouldUpdate: false
		}

		this.toggleUpdate = this.toggleUpdate.bind(this)
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	componentWillMount(){
		this.props.getUserProfileData(this.props.userId)
	}

	handleSearchBarInput(newSearchTerms) {
		this.props.getSkillsBySearchTerm(newSearchTerms)
	}

	handleSearchBarDelete(deleteItem) {
		this.props.getSkillsBySearchTerm(deleteItem, 'delete')
	}

	toggleUpdate(bool) {
		this.setState({
			searchItems
		})
	}

	render() {
		const { handleEdit, skills, user, skillSearchTerms } = this.props
		return (
			<div className="searchbar">
				<p className="subtitle">Neuen Skill hinzufügen</p>
				<p className="search-description">Suche nach Skills, die Du auf Deinem Profil zeigen möchtest</p>
				<SearchBar
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					parent={this}
					searchTerms={skillSearchTerms} />
				{skills.map(skill => {
					return <SkillEditor
						handleEdit={handleEdit}
						skill={skill}
						key={skill}
						user={user} />
				})}
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skills: state.skills,
		user: state.user,
		skillSearchTerms: state.skillSearchTerms
	}
}

export default connect(mapStateToProps, { getSkillsBySearchTerm, getUserProfileData })(SkillSearch)
