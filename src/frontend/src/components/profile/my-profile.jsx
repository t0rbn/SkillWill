import React from 'react'
import { Router, Link, browserHistory } from 'react-router'
import BasicProfile from "./basic-profile.jsx"
import SkillSearch from "../search/skill-search.jsx"
import config from '../../config.json'
import Editor from '../editor/editor.jsx'
import Cookies from 'react-cookie'
import { getUserProfileData, toggleSkillsEditMode, editSkill, setLastSortedBy, updateUserSkills } from '../../actions'
import { connect } from 'react-redux'

class MyProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			session: undefined,
			userId: this.props.params.id,
			data: null,
			dataLoaded: false,
			editLayerOpen: false,
			openLayerAt: -1,
			shouldShowAllSkills: false,
			skillSearchOpen: false,
			skillEditOpen: false
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
			browserHistory.push("/my-profile/login")
		}
		if (!this.checkUser()) {
			browserHistory.push("/my-profile/login")
		}
		document.body.classList.add('my-profile-open')
	}

	componentWillUnmount() {
		document.body.classList.remove('my-profile-open')
	}

	checkAndOpenLogin() {
		const session = this.state.session || Cookies.load("session")
		if (session != this.state.session || !session) {
			this.setState({ session: session })
		}
		return !!session
	}

	checkUser() {
		const { userId } = this.state
		// check if the profiles userID matches with the logged in user
		const user = Cookies.load("user")
		if (user != userId) {
			return false
		}
		return true
	}

	toggleSkillsSearch() {
		this.setState({
			skillSearchOpen: !this.state.skillSearchOpen
		})
	}

	toggleSkillsEdit() {
		this.props.getUserProfileData(this.state.userId)
		this.props.toggleSkillsEditMode()
		this.setState({
			skillEditOpen: !this.state.skillEditOpen
		})
	}

	editSkill(skill, skillLevel, willLevel, isMentor) {
		const { userId, session } = this.state
		if (skillLevel === '0' && willLevel === '0') {
			alert('not allowed')
			return
		}
		let postData = new FormData()
		postData.append("skill", skill)
		postData.append("skill_level", skillLevel)
		postData.append("will_level", willLevel)
		postData.append("session", session)
		postData.append("mentor", isMentor)
		const options = { method: "POST", body: postData, credentials: 'same-origin' }

		this.props.updateUserSkills(options, skill, userId)
	}

	deleteSkill(skill) {
		const { userId, session } = this.state
		const options = { method: "DELETE", credentials: 'same-origin' }
		const requestURL = `${config.backendServer}/users/${userId}/skills?session=${session}&skill=${skill}`
		fetch(requestURL, options)
			.then(res => {
				if (res.status == 401) {
					alert('Session abgelaufen')
					Cookies.remove("session")
					this.setState({
						session: undefined,
						editLayerOpen: false
					})
					this.getProfileData(this)
				}

				if (res.status != 200) {
					throw Error("error while deleting skills")
				}
				else {
					this.getProfileData(this)
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
			userId
		} = this.state
		const {userLoaded} = this.props
		return (
			userLoaded ?
				skillSearchOpen ?
					<div className="profile">
						<SkillSearch handleEdit={this.editSkill} userId={userId}/>
						<div className="back-btn" onClick={this.toggleSkillsSearch}></div>
					</div>
					:
					<div className="profile">
						<BasicProfile
							openLayerAt={openLayerAt}
							shouldShowAllSkills={shouldShowAllSkills}
							checkLogin={this.checkAndOpenLogin}
							editSkill={this.editSkill}
							deleteSkill={this.deleteSkill}
							setLastSortedBy={this.props.setLastSortedBy}
							lastSortedBy={this.props.lastSortedBy} />
						<div className="profile-actions" data-skilledit={skillEditOpen}>
							<button className="edit-skill-btn" onClick={this.toggleSkillsEdit}>
								{skillEditOpen ? 'Fertig' : 'Skills anpassen'}
							</button>
							<button className="add-skill-btn" onClick={this.toggleSkillsSearch} disabled={skillEditOpen}>
								Skill hinzufügen
							</button>
						</div>
					</div>
				: null
		)
	}
}
function mapStateToProps(state) {
	return {
		userLoaded: state.user.userLoaded,
		lastSortedBy: state.lastSortedBy,
	}
}
export default connect(mapStateToProps, { getUserProfileData, toggleSkillsEditMode, editSkill, setLastSortedBy, updateUserSkills })(MyProfile)