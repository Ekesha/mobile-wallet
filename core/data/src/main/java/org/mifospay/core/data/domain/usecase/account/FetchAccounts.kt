/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-wallet/blob/master/LICENSE.md
 */
package org.mifospay.core.data.domain.usecase.account

import com.mifospay.core.model.domain.Account
import com.mifospay.core.model.entity.client.ClientAccounts
import org.mifospay.core.data.base.UseCase
import org.mifospay.core.data.fineract.entity.mapper.AccountMapper
import org.mifospay.core.data.fineract.repository.FineractRepository
import org.mifospay.core.data.util.Constants
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class FetchAccounts @Inject constructor(
    private val fineractRepository: FineractRepository,
    private val accountMapper: AccountMapper,
) : UseCase<FetchAccounts.RequestValues, FetchAccounts.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues) {
        fineractRepository.getAccounts(requestValues.clientId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                object : Subscriber<ClientAccounts?>() {
                    override fun onCompleted() {}
                    override fun onError(e: Throwable) {
                        useCaseCallback.onError(Constants.ERROR_FETCHING_ACCOUNTS)
                    }

                    override fun onNext(t: ClientAccounts?) {
                        if (t != null) {
                            useCaseCallback.onSuccess(
                                ResponseValue(
                                    accountMapper.transform(t),
                                ),
                            )
                        } else {
                            useCaseCallback.onError(Constants.NO_ACCOUNTS_FOUND)
                        }
                    }
                },
            )
    }

    data class RequestValues(val clientId: Long) : UseCase.RequestValues
    data class ResponseValue(val accountList: List<Account>) : UseCase.ResponseValue
}
