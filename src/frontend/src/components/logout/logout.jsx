import React from 'react'
import { apiServer } from '../../env.js'
import Cookies from 'react-cookie'
import { Router, Link, browserHistory } from 'react-router'
import { connect } from 'react-redux'
import { clearUserData } from '../../actions'

class Logout extends React.Component {
	constructor(props) {
		super(props)

		this.generatePostData = this.generatePostData.bind(this)
		this.removeCookies = this.removeCookies.bind(this)
		this.requestLogout = this.requestLogout.bind(this)
	}

	generatePostData() {
		const sessionKey = Cookies.load("sessionKey")
		const postData = new FormData()
		postData.append("sessionKey", sessionKey)
		return postData
	}

	removeCookies() {
		Cookies.remove('sessionKey', { path: '/' })
		Cookies.remove('user', { path: '/' })
	}

	requestLogout(postData) {
		const options = {
			method: "POST",
			body: postData,
			credentials: 'same-origin'
		}
		fetch(`${apiServer}/logout`, options).then(response => {
			this.removeCookies()
			this.setState({
				userId: undefined,
				user: undefined,
			})
			this.props.clearUserData()
			browserHistory.push('/')
		})
	}

	componentWillMount() {
		const postData = this.generatePostData()
		this.requestLogout(postData)
	}

	render() {
		return null
	}
}

export default connect(null, { clearUserData })(Logout)
