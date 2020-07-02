require([
    'ajax',
    'src/modules/raven',
    'src/modules/analytics/setup',
    'src/modules/welcome',
    'src/modules/slideshow',
    'src/modules/images',
    'src/modules/toggle',
    'src/modules/dropdown',
    'src/modules/sticky',
    'src/modules/sectionNav',
    'src/modules/navigation',
    'src/modules/userDetails',
    'src/modules/videoOverlay',
    'src/modules/modal',
    'src/modules/events/cta',
    'src/modules/events/remainingTickets',
    'src/modules/events/eventPriceEnhance',
    'src/modules/filterFacets',
    'src/modules/filterLiveSearch',
    'src/modules/form',
    'src/modules/form/processSubmit',
    'src/modules/identityMenu/setup',
    'src/modules/comparisonTable',
    'src/modules/metrics',
    'src/modules/patterns',
    'src/modules/memstatus',
    'src/modules/faq',
    'src/modules/landingBundles',
    'src/modules/bundlesLanding',
    'src/modules/consentBanner',
    '../utils/cookie',
    '@guardian/consent-management-platform'
], function(
    ajax,
    raven,
    analytics,
    welcome,
    slideshow,
    images,
    toggle,
    dropdown,
    sticky,
    sectionNav,
    navigation,
    userDetails,
    videoOverlay,
    modal,
    cta,
    remainingTickets,
    eventPriceEnhance,
    filterFacets,
    filterLiveSearch,
    form,
    processSubmit,
    identityMenu,
    comparisonTable,
    metrics,
    patterns,
    memstatus,
    faq,
    landingBundles,
    bundlesLanding,
    consentBanner,
    cookie,
    cmp
) {
    'use strict';

    const countryId = cookie.getCookie('GU_country');

    /**
     * If in US initialise CMP as early as possible so
     * subsequent call to onIabConsentNotification
     * returns the correct consentState.
    * */
    if (countryId === 'US') {
        cmp.init({
            useCcpa: true,
        });
    } else {
        consentBanner.init();
    }

    ajax.init({page: {ajaxUrl: ''}});
    raven.init('https://d35a3ab8382a49889557d312e75b2179@sentry.io/1218929');
    analytics.init();

    // Global
    welcome.init();
    slideshow.init();
    images.init();
    toggle.init();
    dropdown.init();
    sticky.init();
    sectionNav.init();
    identityMenu.init();
    navigation.init();
    userDetails.init();
    videoOverlay.init();
    modal.init();
    comparisonTable.init();

    // Events
    cta.init();
    remainingTickets.init();
    eventPriceEnhance.init();

    // Filtering
    filterFacets.init();
    filterLiveSearch.init();

    // Forms
    form.init();
    processSubmit.init();

    // Metrics
    metrics.init();

    //Landing Bundles
    landingBundles.init();
    bundlesLanding.init();

    // Pattern library
    patterns.init();

    memstatus.init();

    faq.init();
});
