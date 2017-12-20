function option(value, display) {
  display = display == undefined ? value : display
  return {
    value,
    display
  }
}

const allLocations = option('all', 'all locations')

const allCompanies = option('all', 'all companies')

const hamburg = option('Hamburg')

const frankfurt = option('Frankfurt')

const munich = option('München')

const berlin = option('Berlin')

const prague = option('Prag')

export const companiesFilterOptions = [
  allCompanies,
  option('S2 Germany'),
  option('S2 Swipe'),
  option('S2 Commerce'),
  option('S2 Content'),
  option('S2 AG')
]

export const locationOptionsForCompany = (company) => {
  switch(company) {
    case 'S2 Germany':
      return [allLocations, hamburg, frankfurt, munich, prague, berlin]
    case 'S2 Swipe':
      return [allLocations, hamburg, berlin]
    case 'S2 Commerce':
      return [allLocations, hamburg, prague]
    case 'S2 Content':
      return [allLocations, hamburg]
    case 'S2 AG':
      return [allLocations, hamburg]
    default:
      return [allLocations, hamburg, frankfurt, munich, prague, berlin]
  }
}
