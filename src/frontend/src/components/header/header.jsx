import React from 'react'
import { Link } from 'react-router'
import Icon from '../icon/icon.jsx'
import config from '../../config.json'

export default class Header extends React.Component {
	render() {
		return (
			<header className="mod-navigation-container">
				<div className="mod-navigation">
					<Link className="s2-logo" to="/">
						<Icon name="s2-logo" width={131} height={30} />
					</Link>
					<Link className="sw-title" to="/">
						skill/will
					</Link>
					<nav className="nav">
						<ul className="nav-list">
							<li className="nav-item">
								<a className="nav-link" href={config.slackChannelUrl}>Slack channel</a>
							</li>
							<li className="nav-item">
								<Link
									className="nav-link nav-link--profile"
									to={'/my-profile/'}
								>
									Your profile
								</Link>
							</li>
						</ul>
					</nav>
				</div>
			</header>
		)
	}
}
