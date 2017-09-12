import React from 'react'
import { browserHistory } from 'react-router'
import BasicProfile from './basic-profile.jsx'
import SkillSearch from '../search/skill-search.jsx'
import { apiServer } from '../../env.js'
import Cookies from 'react-cookie'
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
			sessionKey: null,
			userId: this.props.params.id,
			data: null,
			dataLoaded: false,
			editLayerOpen: false,
			openLayerAt: -1,
			shouldShowAllSkills: false,
			skillSearchOpen: false,
			skillEditOpen: false,
		}
		this.checkAndOpenLogin = this.checkAndOpenLogin.bind(this)
		this.toggleSkillsSearch = this.toggleSkillsSearch.bind(this)
		this.toggleSkillsEdit = this.toggleSkillsEdit.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
	}

	componentWillMount() {
		this.props.getUserProfileData(this.state.userId)
		if (!this.checkAndOpenLogin()) {
			browserHistory.push('/my-profile/login')
		}
		if (!this.checkUser()) {
			browserHistory.push('/my-profile/login')
		}
		document.body.classList.add('my-profile-open')
	}

	componentWillUnmount() {
		this.props.exitSkillsEditMode()
		document.body.classList.remove('my-profile-open')
	}

	checkAndOpenLogin() {
		const sessionKey = this.state.sessionKey || Cookies.load('sessionKey')
		if (sessionKey !== this.state.sessionKey || !sessionKey) {
			this.setState({ sessionKey: sessionKey })
		}
		return !!sessionKey
	}

	checkUser() {
		const { userId } = this.state
		// check if the profiles userID matches with the logged in user
		const user = Cookies.load('user')
		if (user !== userId) {
			return false
		}
		return true
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
	}

	editSkill(skill, skillLevel, willLevel, isMentor = false) {
		const { userId, sessionKey } = this.state
		if (skillLevel === '0' && willLevel === '0') {
      alert('not allowed') // eslint-disable-line
			return
		}
		let postData = new FormData()
		postData.append('skill', skill)
		postData.append('skill_level', skillLevel)
		postData.append('will_level', willLevel)
		postData.append('sessionKey', sessionKey)
		postData.append('mentor', isMentor)
		const options = {
			method: 'POST',
			body: postData,
			credentials: 'same-origin',
		}

		this.props.updateUserSkills(options, userId)
	}

	deleteSkill(skill) {
		const { userId, sessionKey } = this.state
		const options = { method: 'DELETE', credentials: 'same-origin' }
		const requestURL = `${apiServer}/users/${userId}/skills?sessionKey=${sessionKey}&skill=${skill}`
		fetch(requestURL, options)
			.then(res => {
				if (res.status === 403) {
          alert('sessionKey abgelaufen') // eslint-disable-line
					Cookies.remove('sessionKey')
					this.setState({
						sessionKey: null,
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
		const { userLoaded } = this.props

		return userLoaded ? skillSearchOpen ? (
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
						Customise skills
					</button>
					<button className="add-skill-btn" onClick={this.toggleSkillsSearch}>
						Done
					</button>
				</div>
			</div>
		) : (
			<div className="profile">
				<BasicProfile
					openLayerAt={openLayerAt}
					shouldShowAllSkills={shouldShowAllSkills}
					checkLogin={this.checkAndOpenLogin}
					editSkill={this.editSkill}
					deleteSkill={this.deleteSkill}
					setLastSortedBy={this.props.setLastSortedBy}
					lastSortedBy={this.props.lastSortedBy}
				/>
				<div className="profile-actions" data-skilledit={skillEditOpen}>
					<button className="edit-skill-btn" onClick={this.toggleSkillsEdit}>
						{skillEditOpen ? 'Done' : 'Customise skills'}
					</button>
					<button
						className="add-skill-btn"
						onClick={this.toggleSkillsSearch}
						disabled={skillEditOpen}>
						Add new skill
					</button>
				</div>
			</div>
		) : null
	}
}
function mapStateToProps(state) {
	return {
		userLoaded: state.user.userLoaded,
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
