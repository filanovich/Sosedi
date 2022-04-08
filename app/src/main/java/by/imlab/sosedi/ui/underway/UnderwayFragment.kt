package by.imlab.sosedi.ui.underway

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import kotlinx.android.synthetic.main.underway_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class UnderwayFragment : Fragment(R.layout.underway_fragment) {

    private val viewModel: UnderwayViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Fetch underway order
        viewModel.fetchUnderwayOrder()

        // Show logout dialog when clicked on back press button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //dialog(R.string.you_have_pending_orders, R.string.complete, {
                    //findNavController().navigate(R.id.loginFragment)
                //}, R.string.cancel, {})
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Check fetch underway state
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }
    }

    private fun render(state: UnderwayState) {
        when (state) {
            is UnderwayState.OrderSuccess -> renderSuccess(orderWithEntities = state.orderWithEntities)
            is UnderwayState.OrderError -> renderError()
        }
    }

    private fun renderSuccess(orderWithEntities: OrderWithEntities) {
        // Collection button click listener
        collectionButton.setOnClickListener {
            val action = if (orderWithEntities.cargoSpaceList.isEmpty()) {
                UnderwayFragmentDirections.actionUnderwayFragmentToOpenPackageFragment()
            } else {
                UnderwayFragmentDirections.actionUnderwayFragmentToOrderDetailsFragment()
            }
            findNavController().navigate(action)
        }

        // Specification button click listener
        specificationButton.setOnClickListener {
            val action = UnderwayFragmentDirections
                .actionUnderwayFragmentToSpecificationFragment(
                    orderId = orderWithEntities.order.id,
                    canEdit = true
                )
            findNavController().navigate(action)
        }
    }

    private fun renderError() {
        if (isResumed && isVisible && lifecycle.currentState == Lifecycle.State.RESUMED) {
            dialog(
                title = R.string.no_underway_orders,
                message = R.string.take_another_order_to_underway,
                positiveText = R.string.go_back
            ) { findNavController().navigate(R.id.ordersListFragment) }
        }
    }

}