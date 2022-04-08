package by.imlab.sosedi.ui.login

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.network.model.Update
import by.imlab.sosedi.BuildConfig
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.checkNetworkState
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.toast
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.collected_spec_fragment.*
import kotlinx.android.synthetic.main.current_sku_view.*
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.navigation_header.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.content.FileProvider

import android.os.Build
import by.imlab.data.preferences.Prefs
import by.imlab.sosedi.ui.global.extentions.hideKeyboard
import by.imlab.sosedi.ui.underway.UnderwayFragmentDirections
import java.io.File


class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginViewModel by viewModel()
    private var spotsDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textVersion.text = BuildConfig.VERSION_NAME

        spotsDialog = SpotsDialog.Builder()
            .setCancelable(false)
            .setContext(requireContext()).build()

        // Use username from the last session
        username.setText(getLastUsername(), TextView.BufferType.EDITABLE)

        // Focus password field if the last username is not empty
        if (getLastUsername().isNotEmpty()) {
            password.requestFocus()
        }

        // Check login state
        viewModel.loginState.observe(viewLifecycleOwner) { state -> render(state = state) }

        // Listen done button on the keyboard
        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndLogin()
            }
            false
        }

        // Check back button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Login button progress binding and click listener
        bindProgressButton(loginButton)
        loginButton.setOnClickListener {
            hideKeyboard()
            validateAndLogin()
        }

        //settingsButton.setOnClickListener {
//            findNavController().navigate(R.id.settingsFragment)
  //      }

        // Init dots menu
        val popupMenu = PopupMenu(requireContext(), loginDotsMenu)
        popupMenu.menuInflater.inflate(R.menu.login_menu, popupMenu.menu)

        // Dots menu click listener
        loginDotsMenu.setOnClickListener { popupMenu.show() }

        // Dots menu item click listener
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToSettingsFragment()
                    findNavController().navigate(action)
                }
                R.id.check_update -> viewModel.checkUpdate()
            }
            return@setOnMenuItemClickListener true
        }

        viewModel.logout(requireContext())
    }

    private fun render(state: LoginState) {
        when (state) {
            is LoginState.DataValid -> renderDataValid()
            is LoginState.UsernameError -> renderUsernameError()
            is LoginState.PasswordError -> renderPasswordError()
            is LoginState.LoginSuccess -> renderLoginSuccess()
            is LoginState.LoginFailed -> renderLoginFailed(state.message)
            is LoginState.LogoutFailed -> renderLogoutFailed(state.message)
            is LoginState.Loading -> renderLoading()
            is LoginState.CheckUpdateSuccess -> renderCheckUpdateSuccess(state.update)
            is LoginState.InstallUpdate -> installUpdate(state.apkFile)

            is LoginState.SpotsDialogOn -> spotsDialog?.show()
        }
    }

    private fun renderDataValid() {
        viewModel.login(
            requireContext(),
            username = username.text.toString(),
            password = password.text.toString()
        )
    }

    private fun renderUsernameError() {
        toast(R.string.invalid_username)
    }

    private fun renderPasswordError() {
        toast(R.string.invalid_password)
    }

    private fun renderLoginSuccess() {
        // Save username to use the next session
        saveLastUsername()

        // Hide progress on the button
        loginButton.hideProgress(newTextRes = R.string.enter)

        try {
            val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val action = LoginFragmentDirections.actionLoginFragmentToSearchPrinterFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            val action = LoginFragmentDirections.actionLoginFragmentToOrdersListFragment()
            findNavController().navigate(action)
        }
    }

    private fun renderLoginFailed(message: String?) {

        spotsDialog?.dismiss()

        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }
        //toast(R.string.login_failed)
        loginButton.hideProgress(newTextRes = R.string.enter)
    }

    private fun renderLogoutFailed(message: String?) {
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }
    }

    private fun renderLoading() {
        loginButton.showProgress {
            progressColor = Color.WHITE
        }
    }

    private fun renderCheckUpdateSuccess(update: Update) {

        val versionCode = BuildConfig.VERSION_CODE;
        val newVersionCode = update.versionCode;

        spotsDialog?.dismiss()

        if (newVersionCode > versionCode) {
            dialog(
                title = getString(R.string.check_update),
                message = getString(R.string.updater_available, update.versionName),
                positiveText = getString(android.R.string.yes), {
                    viewModel.downloadAndInstallUpdate(update.outputFile)
                },
                negativeText = getString(android.R.string.no), {}
            )
        } else {
            dialog(
                title = getString(R.string.check_update),
                message = getString(R.string.updater_latest),
                positiveText = getString(R.string.ok)
            ) {}
        }
    }

    private  fun installUpdate(apkFile: String) {

        spotsDialog?.dismiss()

        val file = File(Prefs.filesDir + apkFile)

        val intent = Intent(Intent.ACTION_VIEW)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            val contentUri = FileProvider.getUriForFile(requireActivity(),
                requireActivity().applicationContext.packageName + ".provider", file)

            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")

            //val resInfoList: List<ResolveInfo> = requireActivity().packageManager
            //    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            //f/or (resolveInfo in resInfoList) {
            //    requireActivity().grantUriPermission(
            //        requireActivity().applicationContext.packageName + ".provider",
            //        contentUri,
            //        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            //    )
            //}
            intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION

            requireActivity().startActivity(intent)

//            val contentUri = FileProvider.getUriForFile(
//                requireContext(),
//                BuildConfig.APPLICATION_ID + ".provider",
//                file
//            )
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setDataAndType(contentUri,  "application/vnd.android.package-archive")
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            //intent.data = contentUri
//            requireActivity().startActivity(intent)
            requireActivity().finish()
        } else {
            val apkUri: Uri = Uri.fromFile(file)
            //var intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().startActivity(intent)
            // finish()
        }
    }

    private fun validateAndLogin() {
        if (requireContext().checkNetworkState()) {
            viewModel.validateCredentials(
                username = username.text.toString(),
                password = password.text.toString()
            )
        } else {
            toast(R.string.internet_is_disabled, duration = Toast.LENGTH_LONG)
        }
    }

    private fun saveLastUsername() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(LAST_USERNAME, username.text.toString())
            commit()
        }

        // Set to nav bar header
        requireActivity().nameAndSurname.text = username.text.toString()
    }

    private fun getLastUsername(): String {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getString(LAST_USERNAME, "") ?: ""
    }

    companion object {
        private const val LAST_USERNAME = "LAST_USERNAME"
    }
}