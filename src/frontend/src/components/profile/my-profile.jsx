import React from 'react'
import BasicProfile from './basic-profile.jsx'
import SkillSearch from '../search/skill-search.jsx'
import Icon from '../icon/icon.jsx'
import Layer from "../layer/layer"
import { apiServer } from '../../env.js'
import {
	getUserProfileData,
	toggleSkillsEditMode,
	exitSkillsEditMode,
	editSkill,
	setLastSortedBy,
	updateUserSkills,
} from '../../actions'
import { connect } from 'react-redux'

class MyProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			userId: this.props.user.id,
			data: null,
			dataLoaded: false,
			editLayerOpen: false,
			openLayerAt: -1,
			shouldShowAllSkills: true,
			skillSearchOpen: false,
			skillEditOpen: false,
		}
		this.toggleSkillsSearch = this.toggleSkillsSearch.bind(this)
		this.toggleSkillsEdit = this.toggleSkillsEdit.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
	}

	componentWillMount() {
		// this.props.getUserProfileData(this.state.userId)
		if (this.props.user.userLoaded) {
			document.body.classList.add('my-profile-open')
		}
	}

	componentWillUnmount() {
		this.props.exitSkillsEditMode()
		document.body.classList.remove('my-profile-open')
	}

	toggleSkillsSearch() {
		this.props.getUserProfileData(this.state.userId)
		this.setState({
			skillSearchOpen: !this.state.skillSearchOpen,
		})
	}

	toggleSkillsEdit() {
		this.props.getUserProfileData(this.state.userId)
		this.props.toggleSkillsEditMode()
		this.setState({
			skillEditOpen: !this.state.skillEditOpen,
		})
		document.body.classList.toggle('is-edit-mode')
	}

	editSkill(skill, skillLevel, willLevel, isMentor = false) {
		const { userId } = this.state
		if (skillLevel === '0' && willLevel === '0') {
      alert('Please select a value greater than 0') // eslint-disable-line
			return
		}
		let postData = new FormData()
		postData.append('skill', skill)
		postData.append('skill_level', skillLevel)
		postData.append('will_level', willLevel)
		postData.append('mentor', isMentor)
		const options = {
			method: 'POST',
			body: postData,
			credentials: 'same-origin',
		}

		this.props.updateUserSkills(options, userId)
	}

	deleteSkill(skill) {
		const { userId } = this.state
		const options = { method: 'DELETE', credentials: 'same-origin' }
		const requestURL = `${apiServer}/users/${userId}/skills?skill=${encodeURIComponent(
			skill
		)}`
		fetch(requestURL, options)
			.then(res => {
				if (res.status === 403) {
          alert('session invalid') // eslint-disable-line
					this.setState({
						editLayerOpen: false,
					})
					this.props.getUserProfileData(userId)
				}

				if (res.status !== 200) {
					throw Error('error while deleting skills')
				} else {
					this.props.getUserProfileData(userId)
				}
			})
			.catch(err => console.log(err))
	}

	render() {
		const {
			skillSearchOpen,
			openLayerAt,
			shouldShowAllSkills,
			skillEditOpen,
			userId,
		} = this.state
		const { userLoaded } = this.props.user
		return userLoaded ? (
			skillSearchOpen ? (
				<Layer>
					<div className="profile">
						<SkillSearch
							handleEdit={this.editSkill}
							handleDelete={this.deleteSkill}
							userId={userId}
						/>
						<div className="profile-actions" data-skillsearch={skillSearchOpen}>
							<button
								className="edit-skill-btn"
								onClick={this.toggleSkillsEdit}
								disabled={skillSearchOpen}>
								<Icon name="edit" size={19} />
								Customize skills
							</button>
							<button className="add-skill-btn" onClick={this.toggleSkillsSearch}>
								<Icon name="checkmark" size={19} />
								Done
							</button>
						</div>
					</div>
				</Layer>
				) : (
				<Layer>
					<div className="profile">
						<BasicProfile
							openLayerAt={openLayerAt}
							shouldShowAllSkills={shouldShowAllSkills}
							editSkill={this.editSkill}
							deleteSkill={this.deleteSkill}
							setLastSortedBy={this.props.setLastSortedBy}
							lastSortedBy={this.props.lastSortedBy}
							getUserProfileData={this.props.getUserProfileData}
						/>
						<div className="profile-actions" data-skilledit={skillEditOpen}>
							<button className="edit-skill-btn" onClick={this.toggleSkillsEdit}>
								{skillEditOpen ? (
									<Icon name="checkmark" size={18} />
								) : (
									<Icon name="edit" size={18} />
								)}
								{skillEditOpen ? 'Done' : 'Customize skills'}
							</button>
							<button
								className="add-skill-btn"
								onClick={this.toggleSkillsSearch}
								disabled={skillEditOpen}>
								<Icon name="plus" size={18} />
								Add new skill
							</button>
						</div>
					</div>
				</Layer>
			)
		) : null
	}
}
function mapStateToProps(state) {
	return {
		user: state.user,
		lastSortedBy: state.lastSortedBy,
	}
}
export default connect(mapStateToProps, {
	getUserProfileData,
	toggleSkillsEditMode,
	exitSkillsEditMode,
	editSkill,
	setLastSortedBy,
	updateUserSkills,
})(MyProfile)
