import styles from './styles.less'
import React from 'react'
import Header from './components/header/header.jsx'
import Footer from './components/footer/footer.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'

export default class App extends React.Component {
	render() {
		return (
			<div>
				<Header />
				<div className="background-layer"></div>
				<div className="content">
					<h1 className="title">SkillWill</h1>
					<h3 className="subtitle">Wir haben Talent</h3>
					<UserSearch location={this.props.location} />
					<Results />
					{this.props.children}
				</div>
				<Footer />
			</div>
		)
	}
}
