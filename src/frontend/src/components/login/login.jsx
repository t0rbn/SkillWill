import React from 'react'
import { apiServer } from '../../env.js'
import Cookies from 'react-cookie'
import { browserHistory } from 'react-router'
import { getUserProfileData } from '../../actions'
import { connect } from 'react-redux'

class Login extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			user: Cookies.load('user'),
			password: null,
			errormessage: null,
			isUserLogedIn: false,
		}

		this.handleUserchange = this.handleUserchange.bind(this)
		this.handlePasswordChange = this.handlePasswordChange.bind(this)
		this.handleLogin = this.handleLogin.bind(this)
		this.retrievesessionKey = this.retrievesessionKey.bind(this)
		this.saveCookies = this.saveCookies.bind(this)
		this.generatePostData = this.generatePostData.bind(this)
		this.handleResponseStatus = this.handleResponseStatus.bind(this)
	}

	componentWillMount() {
		if (this.state.user) {
			browserHistory.push(`/my-profile/${this.state.user}`)
		}
	}

	handleUserchange(e) {
		this.setState({ user: e.target.value })
	}

	handlePasswordChange(e) {
		this.setState({ password: e.target.value })
	}

	generatePostData() {
		const postData = new FormData()
		postData.append('username', this.state.user)
		postData.append('password', this.state.password)
		return postData
	}

	saveCookies(sessionKey) {
		Cookies.save('sessionKey', sessionKey, { path: '/', maxAge: 50400 })
		Cookies.save('user', this.state.user, { path: '/', maxAge: 50400 })
	}

	retrievesessionKey(sessionKey) {
		if (!sessionKey) {
			throw Error('sessionKey is unknown')
		}
		this.saveCookies(sessionKey)
		this.setState({
			loginLayerOpen: false,
			sessionKey: sessionKey,
			isUserLogedIn: true,
		})
		this.props.getUserProfileData(this.state.user)
		browserHistory.push(`/my-profile/${this.state.user}`)
	}

	handleResponseStatus(response) {
		if (response.status === 200) {
			this.setState({
				password: null,
				errormessage: null,
			})
			return true
		} else if (response.status === 403) {
			this.setState({
				password: null,
				errormessage: 'User/Passwort falsch',
			})
			throw Error(response.statusText)
		} else {
			this.setState({
				user: null,
				password: null,
				errormessage: 'Da ging was nicht',
			})
			throw Error(response.statusText)
		}
	}

	handleLogin(event) {
		event.preventDefault()
		const postData = this.generatePostData()
		const options = {
			method: 'POST',
			body: postData,
			credentials: 'same-origin',
		}
		fetch(`${apiServer}/login`, options)
			.then(response => {
				if (this.handleResponseStatus(response)) {
					return response.json()
				}
			})
			.then(data => this.retrievesessionKey(data.sessionKey))
			.catch(err => console.log(err))
	}

	render() {
		return (
			<div className="login">
				<h1 className="subtitle">{"Hang on, you haven't logged in yet!"}</h1>
				<form onSubmit={this.handleLogin}>
					<input
						name="username"
						placeholder="LDAP User"
						type="text"
						spellCheck="false"
						value={this.state.user || ''}
						onChange={this.handleUserchange}
					/>
					<input
						name="password"
						placeholder="Password"
						type="password"
						spellCheck="false"
						value={this.state.password || ''}
						onChange={this.handlePasswordChange}
					/>
					<button className="submit-btn" type="submit">
						Login
					</button>
					<p className="error">{this.state.errormessage}</p>
				</form>
			</div>
		)
	}
}

export default connect(null, { getUserProfileData })(Login)
