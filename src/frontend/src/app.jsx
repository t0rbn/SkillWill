import styles from './styles.less'
import React from 'react'
import Header from './components/header/header.jsx'
import Footer from './components/footer/footer.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'
import { connect } from 'react-redux'

class App extends React.Component {
	render() {
		const { isResultsLoaded, isSkillAnimated } = this.props
		return (
			<div className={isResultsLoaded ? "results-loaded" : ""}>
				<Header />
				<div className="search">
					<div className="heading">
						<h1 className="title">skill/will</h1>
						<h3 className="subtitle">We have talent!</h3>
					</div>
					<div className="container">
						<UserSearch location={this.props.location} />
					</div>
				</div>
				<div className="content">
					<Results animated={isSkillAnimated}/>
					{this.props.children}
				</div>
				<Footer />
				<div className="layer-overlay"></div>
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		isResultsLoaded: state.isResultsLoaded,
		isSkillAnimated: state.isSkillAnimated
	}
}
export default connect(mapStateToProps)(App)