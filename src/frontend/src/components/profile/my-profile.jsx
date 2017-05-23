import React from 'react'
import { Router, Link, browserHistory } from 'react-router'
import BasicProfile from "./basic-profile.jsx"
import SkillSearch from "../search/skill-search.jsx"
import config from '../../config.json'
import Editor from '../editor/editor.jsx'
import Cookies from 'react-cookie'
import { getUserProfileData } from '../../actions'
import { connect } from 'react-redux'

class MyProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			session: undefined,
			userId: "id",
			data: null,
			dataLoaded: false,
			editLayerOpen: false,
			openLayerAt: -1,
			shouldShowAllSkills: false,
			skillSearchOpen: false
		}
		this.infoLayer = this.infoLayer.bind(this)
		this.openCloseEditLayer = this.openCloseEditLayer.bind(this)
		this.checkAndOpenLogin = this.checkAndOpenLogin.bind(this)
		this.openCloseSkillSearch = this.openCloseSkillSearch.bind(this)
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
		this.getProfileData = this.getProfileData.bind(this)

		if (!this.checkAndOpenLogin()) {
			browserHistory.push("/my-profile/login")
		}
	}

	componentWillMount() {
		this.setState({
			userId: this.props.params.id
		})
		// TODO -> remove both request types
		this.getProfileData()
		this.props.getUserProfileData(this.props.params.id)
		if (!this.checkUser()) {
			browserHistory.push("/my-profile/login")
		}
	}

	getProfileData() {
		fetch(config.backendServer + "/users/" + this.state.userId)
			.then(r => r.json())
			.then(data => {
				this.setState({
					data: data,
					dataLoaded: true
				})
			})
			.catch(function (error) {
				console.error(error)
			})
	}

	checkAndOpenLogin() {
		const session = this.state.session || Cookies.load("session")
		if (session != this.state.session || !session) {
			this.setState({ session: session })
		}
		return !!session
	}

	checkUser() {
		// check if the profiles userID matches with the logged in user
		const user = Cookies.load("user")
		if (user != this.state.userId) {
			return false
		}
		return true
	}

	infoLayer(data, i, shouldShowAllSkills) {
		if (this.state.editLayerOpen && (this.state.openLayerAt == i)) {
			return (
				<Editor
					skillName={data.name}
					skillLvl={data.skillLevel}
					willLvl={data.willLevel}
					handleAccept={this.editSkill}
					handleClose={this.openCloseEditLayer.bind(null)} />
			)
		}
		else {
			return (
				<div class="additional-options">
					<div class="edit" onClick={() => this.openCloseEditLayer(i, shouldShowAllSkills)}></div>
					<div class="delete" onClick={() => this.deleteSkill(data.name)}></div>
				</div>
			)
		}
	}

	openCloseEditLayer(i, show) {
		this.setState({
			openLayerAt: i,
			editLayerOpen: !this.state.editLayerOpen,
			shouldShowAllSkills: show
		})
	}

	openCloseSkillSearch() {
		this.setState({
			skillSearchOpen: !this.state.skillSearchOpen
		})
	}

	editSkill(skill, skillLevel, willLevel) {
		if (skillLevel === '0' && willLevel === '0') {
			alert('not allowed')
			return
		}
		let postData = new FormData()
		postData.append("skill", skill)
		postData.append("skill_level", skillLevel)
		postData.append("will_level", willLevel)
		postData.append("session", this.state.session)

		fetch(config.backendServer + "/users/" + this.state.userId + "/skills", { method: "POST", body: postData })
			.then(res => {
				if (res.status == 401) {
					this.setState({ session: undefined })
					Cookies.remove("session")
					this.editSkill(skill, skillLevel, willLevel)
					this.setState({ editLayerOpen: false })
					this.getProfileData(this)
				}

				if (res.status != 200) {
					throw Error("error while editing skills")
				}
				else {
					this.getProfileData(this)
				}

			})
			.catch(err => console.log(err))
	}

	deleteSkill(skill) {

		fetch(config.backendServer + "/users/" + this.state.userId + "/skills?session=" + this.state.session + "&skill=" + skill, { method: "DELETE" })
			.then(res => {
				if (res.status == 401) {
					this.setState({ session: undefined })
					Cookies.remove("session")
					this.deleteSkill(skill)
					this.setState({ editLayerOpen: false })
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
		return (
			this.state.dataLoaded ?
				this.state.skillSearchOpen ?
					<div class="profile">
						<SkillSearch handleEdit={this.editSkill} />
						<div class="back-btn" onClick={this.openCloseSkillSearch}></div>
					</div>
					:
					<div class="profile">
						<BasicProfile
							user={this.state.data}
							infoLayer={this.infoLayer}
							openLayerAt={this.state.openLayerAt}
							shouldShowAllSkills={this.state.shouldShowAllSkills}
							checkLogin={this.checkAndOpenLogin} />
						<div class="add-skill-btn" onClick={this.openCloseSkillSearch}></div>
					</div>
				: ""
		)
	}
}

export default connect(null, { getUserProfileData })(MyProfile)
